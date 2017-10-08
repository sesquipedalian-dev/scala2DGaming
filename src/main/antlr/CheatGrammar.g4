grammar CheatGrammar;
cheat : changeGMU ;
changeGMU : ('ChangeGMU' | 'cgmu') IntAmt ;

IntAmt : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ;
ErrorCharacter : . ;