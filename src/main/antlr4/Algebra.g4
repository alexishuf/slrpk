grammar Algebra;
@header {
    package com.github.alexishuf.slrpk.algebra.antlr4;
}

prog   : prolog expr;
prolog : (INC file)*;
expr   : expr INT expr
       | expr DIF expr
       | expr UNI expr
       | term ;
term   : COM term | term proj | term fltr | '(' expr ')' | file;
proj   : PROJ COM? ('(' str (',' str)*  ')')?;
fltr   : QST COM? ('(' fldfltr (',' fldfltr)*  ')');
fldfltr: str (EQ|NEQ|MTCHS|NMTCHS) str | COM? str;
str    : UQSTR | SQSTR | DQSTR;
file   : QST? UQSTR | QST? SQSTR | QST? DQSTR;
INT    : WS '&' WS | WS '*' WS;
DIF    : WS '-' WS;
UNI    : WS '|' WS | WS '+' WS;
COM    : '!' | '~';
PROJ   : '#';
QST    : '?';
EQ     : WS  '=' WS;
NEQ    : WS '!=' WS;
MTCHS  : WS  '%' WS;
NMTCHS : WS '!%' WS;
INC    : '#include' WS;
UQSTR  : (~[ \t\r\n()!~#?'"@,])+;
SQSTR  : '\'' ('\\\''|.)+? '\'';
DQSTR  : '"'  ('\\"' |.)+? '"';
WS     : [ \t\r\n]+ -> skip;

