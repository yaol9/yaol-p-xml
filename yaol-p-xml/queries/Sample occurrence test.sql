create table occurrence SELECT
`keyworddewey`.`Keyword`,

count(1) as keywordCount
FROM `dblp`.`keyworddewey` group by 
`keyworddewey`.`Keyword`
;


create table top1000 SELECT * FROM occurrence order  by keywordCount desc limit 1000; 