grammar Fenix;

program: statement* EOF;

statement:
      varDecl//implemented
    | structDeclaration
    | structFunctionDeclaration
    | funcDecl
    | assignmentStatement
    | ifStatement//implemented
    | doWhileStatement
    | whileStatement//implemented
    | forStatement//implemented
    | forEachStatement
    | returnStatement
    | breakStatement//implemented
    | continueStatement//implemented
    | exprStatement
    | block
    ;

structDeclaration: STRUCT_WORD ID '{' structMember* '}';

structMember:
    varDecl
    | MODIFIER* functionTemplate ';'
    ;

functionTemplate: FUNC_WORD ID '(' rawArgs? ')' ('->' TYPE)?;

//io::println(a: Int, b: Int) -> Int {
//     //smth
//}
structFunctionDeclaration: ID '::' ID '(' rawArgs? ')' ('->' TYPE)? statement;

varDecl:
    varDecl_typed ';'
    | varDecl_auto ';'
    ;

varDecl_noSemi:
  varDecl_typed
   | varDecl_auto
   ;

varDecl_typed: VAR_WORD ID ':' TYPE ('=' value=expr)?;
varDecl_auto: AUTO_WORD ID ('=' value=expr)?;

funcDecl: functionTemplate statement;//todo: parameters should have own type!
assignmentStatement: (SELF_WORD '.')? (ID | arrayAccessHelper) '=' expr ';';
arrayAccessHelper: arr=ID ('[' expr ']')*;

doWhileStatement: DO_WORD statement WHILE_WORD '(' expr ')' ';';

ifStatement: IF_WORD '(' expr ')' statement (ELIF_WORD '(' expr ')' statement)* (ELSE_WORD elseSttmt=statement)?; //implemented //todo: add else if
whileStatement: WHILE_WORD '(' expr ')' statement;
forStatement: FOR_WORD '(' forInit? ';' forCondition? ';' forIncrement? ')' statement;
forInit: varDecl_noSemi;
forCondition: expr;
forIncrement: expr;

forEachStatement: FOR_WORD '(' (index=ID ',')? varDecl_noSemi IN_WORD expr ')' statement;

returnStatement: RETURN_WORD expr? ';';
breakStatement: BREAK_WORD ';';
continueStatement: CONTINUE_WORD ';';
exprStatement: expr ';';
block: '{' statement* '}';

expr:
    '-' expr #Negative
    | '!' expr #Not//implemented
    | '(' expr ')' #Parens//implemented
    | ID '++' #PostfixIncrement
    | ID '--' #PostfixDecrement
    | expr '^' expr #Pow//implemented
    | expr '&' expr #BitwiseAnd
    | expr '|' expr #BitwiseOr
    | expr '<<' expr #BitwiseLeft
    | expr '>>' expr #BitwiseRight
    | expr op=('/' | '*' | '%') expr #MulDiv//implemented
    | expr op=('+' | '-') expr #AddSub//implemented
    | expr op=('>' | '>=' | '<=' | '<') expr #Comparsion//implemented
    | expr op=('==' | '!=') expr #Equality//implemented
    | expr '?:' expr #Elvis//todo: more nulleable operators
    | 'new' ID '(' ')' #NewExpr
    | expr '?' expr ':' expr #Ternary
    | expr '&&' expr #And//implemented
    | expr '||' expr #Or//implemented
    | expr '~' expr #Xor//implemented
    | expr IS_WORD TYPE #Is
    | '[' args? ']' #Array
    | ID ('[' expr ']')+ #ArrayAccess
    //todo: selfMethodCall
    | SELF_WORD '.' ID #SelfFieldAccess
    | ID '.' ID '(' args? ')' #StructMemberCallAndObjectFunctionCall
    | ID '(' args? ')' #Call//implemented
    | expr '..' expr (step='..' expr) #Range
    // todo struct field access
    | INT #Int//implemented
    | FLOAT #Float//implemented
    | STRING #String//implemented
    | BOOL #Bool//implemented
    | ID #Var//implemented
    | NULL #Null//implemented
    ;

args: expr (',' expr)*;
rawArgs: arg (',' arg)*;
arg: ID ':' TYPE;//a: Int

RETURN_WORD: 'return';
BREAK_WORD: 'break';
CONTINUE_WORD: 'continue';
FOR_WORD: 'for'; IN_WORD: 'in';
WHILE_WORD: 'while'; DO_WORD: 'do';
IF_WORD: 'if'; ELSE_WORD: 'else'; ELIF_WORD: 'elif';
VAR_WORD: 'var'; AUTO_WORD: 'auto';
FUNC_WORD: 'func';
SELF_WORD: 'self'; STRUCT_WORD: 'struct';
IS_WORD: 'is';

MODIFIER: 'pub' | 'static' | 'loc';

TYPE: ('Int' | 'String' | 'Float' | 'Bool' | 'Null' | 'Obj') ('[' ']')*;

INT: [0-9]+ //dec
    | '0x' [0-9a-fA-F]+ //hex
    | '0b' [01]+ //binary
    | '0o' [0-7]+ //oct
    | [0-9]+ '_' [0-9_]* //1_0_0
    ;

FLOAT: [0-9]+ '.' [0-9]+;
STRING: '"' (~["\\"] | '\\' .)* '"' | '"""' ('"'? ~["\\"] | '\\' .)* '"""';
BOOL: 'true' | 'false';
NULL: 'null' | 'NULL' | 'NIL' | 'nil';
ID: [a-zA-Z_] [a-zA-Z0-9_]*;

LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;