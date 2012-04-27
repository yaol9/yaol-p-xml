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
FROM `dblp`.`keyworddewey_full` where keyword in ('performance','learning','research','machine','mining',
'business','notes','services',
'advanced','methods','complexity',
'platform','digital','graph',
'middleware','communication','automatic',
'process','transactions','digital',
'representation','resource','interface',
'agents','mathematical','reliability',
'collaborative','traffic','evolution',
'functional','visual','fast'
));