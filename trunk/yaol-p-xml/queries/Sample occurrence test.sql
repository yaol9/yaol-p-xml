create table occurrence SELECT
`keyworddewey`.`Keyword`,

sum(1) as keywordCount
FROM `dblp`.`keyworddewey` group by 
`keyworddewey`.`Keyword`
;


create table top1000 SELECT * FROM occurrence order  by keywordCount desc limit 1000; 


drop  TABLE `dblp`.`KeywordDewey`;
CREATE  TABLE `dblp`.`KeywordDewey` (

  `Keyword` VARCHAR(500) NOT NULL ,

  `Dewey` VARCHAR(50) NOT NULL ,

  `Depth` VARCHAR(10) NOT NULL,
`XMLid` INT NOT NULL ,
KEY `index1` (`Keyword`,`XMLid`) USING BTREE
);

insert into `dblp`.`keyworddewey` (select 
`keyworddewey_full`.`Keyword`,
`keyworddewey_full`.`Dewey`,
`keyworddewey_full`.`Depth`,
`keyworddewey_full`.`XMLid`
FROM `dblp`.`keyworddewey_full` where keyword in ('xml','chengfei','jianxin',
'edbt','chengfei','jianxin',
'krishnan','keith','johann','interspeech2003','hypercube',
'interspeech2003','hypercube','mathematicae','masaharu','cache',
'exploration','infrastructure','hirota','hirano','drechsler',
'schneider','church','johnny','attention','movement',
'johnny','attention','trade','entertainment','browsing',
'identifying','church','controllers','notices','engine',
'kazuhiro','seamless','triple','richards','release',
'seamless','triple','richards','hongwei','humans',
'humans','moon','degree','stuart','osamu',
'degree','stuart','alireza','autonomic','collective',
'collective','plant','defined','package','usage',
'valentina','learned','package','usage','phone',
'private','release','transportation','package','usage'));