grammar CheatGrammar;
cheat : changeGMU | changeSoldierNeed | spawnBadGuy | spawnWave | setTimeRate ;
changeGMU : ('ChangeGMU' | 'cgmu') IntAmt ;
changeSoldierNeed : ('ChangeSoldierNeed' | 'csn') ID NeedName IntAmt;
spawnBadGuy : ('SpawnBadGuy' | 'sbg') IntAmt? ;
spawnWave : ('SpawnWave' | 'sw') ;
setTimeRate : ('SetTimeRate' | 'str') (IntAmt | FloatAmt) ;

FloatAmt : [0-9]+ '.' [0-9]+ ;
IntAmt : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ;
NeedName : 'Sleep' | 'Recreation' ;
ID : [a-zA-Z]+ ;
ErrorCharacter : . ;