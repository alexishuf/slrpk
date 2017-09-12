package com.github.alexishuf.slrpk.algebra.runtime;


import com.github.alexishuf.slrpk.Work;
import com.github.alexishuf.slrpk.algebra.*;
import com.github.alexishuf.slrpk.algebra.antlr4.AlgebraBaseVisitor;
import com.github.alexishuf.slrpk.algebra.antlr4.AlgebraLexer;
import com.github.alexishuf.slrpk.algebra.antlr4.AlgebraParser;
import com.github.alexishuf.slrpk.algebra.exceptions.*;
import com.github.alexishuf.slrpk.algebra.predicates.FieldPredicates;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Interpreter {
    private IncludePath includePath = new IncludePath();

    public Interpreter() {
        this.includePath.addIncludeDir(new File(""));
    }

    public @Nonnull IncludePath getIncludePath() {
        return includePath;
    }


    public Set run(@Nonnull String program) {
        CodePointCharStream stream = CharStreams.fromString(program);
        AlgebraLexer lexer = new AlgebraLexer(stream);
        AlgebraParser parser = new AlgebraParser(new CommonTokenStream(lexer));
        return new Visitor().visit(parser.prog());
    }

    private class Visitor extends AlgebraBaseVisitor<Set> {
        private IncludePath includePath = new IncludePath(Interpreter.this.getIncludePath());

        @Override
        public Set visitProg(AlgebraParser.ProgContext ctx) {
            visitProlog(ctx.prolog());
            return visitExpr(ctx.expr());
        }

        @Override
        public Set visitProlog(AlgebraParser.PrologContext ctx) {
            for (AlgebraParser.FileContext fileCtx : ctx.file()) {
                File file = new File(getFilePath(fileCtx));
                if (!file.exists())
                    throw new IncludePathNotFound(file.getPath());
                if (!file.isDirectory())
                    throw new IncludePathNotDir(file.getPath());
                includePath.addIncludeDir(file);
            }
            return null;
        }

        @Override
        @Nonnull
        public Set visitExpr(AlgebraParser.ExprContext ctx) {
            if (ctx.term() != null) return visitTerm(ctx.term());

            Set l = visitExpr(ctx.expr(0));
            Set r = visitExpr(ctx.expr(1));
            boolean lc = l instanceof ComplementSet, rc = r instanceof ComplementSet;

            boolean isINT = ctx.INT() != null || ctx.DIF() != null;
            if (ctx.DIF() != null)
                r = ComplementSet.wrap(r); //bcs x - y <==> x & !y

            if (isINT) {
                if (lc && rc) {
                    l = ((ComplementSet)l).getOperand();
                    r = ((ComplementSet)r).getOperand();
                    return ComplementSet.wrap(new UnionSet(l, r)); //!x & !y <==> !(x | y)
                }
                return new IntersectionSet(l, r);
            } else if (ctx.UNI() != null) {
                if (lc || rc) {
                    //!x | !y <==> !( x &  y)
                    //!x |  y <==> !( x & !y)
                    // x | !y <==> !(!x &  y)
                    l = ComplementSet.wrap(l);
                    r = ComplementSet.wrap(r);
                    return ComplementSet.wrap(new IntersectionSet(l, r));
                }

                return new UnionSet(l, r);
            } else {
                throw new IllegalStateException("Bad interpreter, fix me!");
            }
        }

        @Override
        @Nonnull
        public Set visitTerm(AlgebraParser.TermContext ctx) {
            if (ctx.expr() != null) return visitExpr(ctx.expr());
            else if (ctx.file() != null) return visitFile(ctx.file());
            assert ctx.term() != null;
            Set set = visitTerm(ctx.term());
            if (ctx.COM() != null) return ComplementSet.wrap(set);
            else if (ctx.proj() != null) return visitProj(ctx.proj(), set);
            else if (ctx.fltr() != null) return visitFltr(ctx.fltr(), set);
            else throw new IllegalStateException("Bad interpreter, fix me!");
        }

        public Set visitProj(AlgebraParser.ProjContext ctx, Set set) {
            boolean isComplement = ctx.COM() != null;
            List<String> f = ctx.str().stream().map(this::getString).collect(Collectors.toList());
            return new ProjectedSet(set, f, isComplement);
        }

        public Set visitFltr(AlgebraParser.FltrContext ctx, Set set) {
            Predicate<Work> predicate = ctx.fldfltr().stream().map(this::parseFldfltr)
                    .reduce(Predicate::and).orElse(w -> true);
            if (ctx.COM() != null) predicate = predicate.negate();
            return new FilteredSet(set, predicate);
        }

        public Predicate<Work> parseFldfltr(AlgebraParser.FldfltrContext ctx) {
            String field = getString(ctx.str(0));
            if (ctx.str().size() == 1) {
                return ctx.COM() == null ? FieldPredicates.createNotNull(field)
                                         : FieldPredicates.createNull(field);
            } else {
                String arg = getString(ctx.str(1));
                if (ctx.EQ() != null) {
                    return FieldPredicates.createEquals(field, arg);
                } else if (ctx.NEQ() != null) {
                    return FieldPredicates.createNotEquals(field, arg);
                } else if (ctx.MTCHS() != null || ctx.NMTCHS() != null) {
                    Pattern pattern;
                    try {
                        pattern = Pattern.compile(arg);
                    } catch (PatternSyntaxException e) {
                        throw new InterpretationException("Bad regex", e);
                    }
                    if (ctx.MTCHS() != null)
                        return FieldPredicates.createMatches(field, pattern);
                    if (ctx.NMTCHS() != null)
                        return FieldPredicates.createNotMatches(field, pattern);
                }
            }
            throw new IllegalStateException("Bad interpreter, fix me!");
        }

        @Override
        public Set visitFile(AlgebraParser.FileContext ctx) {
            String path = getFilePath(ctx);
            File file = includePath.resolve(path);
            if (file == null) {
                if (ctx.QST() != null)
                    return new EmptySet();
                else
                    throw new SetFileNotFoundException(path, includePath);
            }
            return loadSet(file);
        }

        private String getFilePath(AlgebraParser.FileContext ctx) {
            return getString(ctx.UQSTR(), ctx.SQSTR(), ctx.DQSTR());
        }

        private String getString(AlgebraParser.StrContext ctx) {
            return getString(ctx.UQSTR(), ctx.SQSTR(), ctx.DQSTR());
        }
        private String getString(TerminalNode uq, TerminalNode sq, TerminalNode dq) {
            String path;
            if (uq != null) {
                path = uq.getText();
            } else {
                if (sq != null) path = sq.getText();
                else if (dq != null) path = dq.getText();
                else throw new IllegalStateException("Bad interpreter, fix me!");
                path = path.substring(1, path.length()-1);
            }
            return path;
        }
    }

    private @Nonnull Set loadSet(File file) throws SetFileLoadException {
        if (file.getName().toLowerCase().endsWith(".bib"))
            return new BibSet(file);
        if (file.getName().toLowerCase().endsWith(".csv"))
            return new CsvSet(file);
        throw new UnknownSetFileExtension(file);
    }
}
