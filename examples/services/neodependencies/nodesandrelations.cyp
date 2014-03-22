MERGE (a: ACOMPANY_Service {name:"company-services", org:"a.company"});
MERGE (a: ACOMPANY {name:"company-db", org:"a.company"});
MERGE (a: External {name:"c3p0", org:"c3p0"});
MERGE (a: External {name:"logback-classic", org:"ch.qos.logback"});
MERGE (a: External {name:"scala-library", org:"org.scala-lang"});
MATCH (a {name:"c3p0", org:"c3p0"}), (b {name:"company-services", org:"a.company"}) CREATE UNIQUE (b)-[r:Depends {version:"0.9.1.2"}]->(a);
MATCH (a {name:"company-db", org:"a.company"}), (b {name:"company-services", org:"a.company"}) CREATE UNIQUE (b)-[r:Depends {version:"1.0", scalaVersion:"2.9.2"}]->(a);
MATCH (a {name:"logback-classic", org:"ch.qos.logback"}), (b {name:"company-services", org:"a.company"}) CREATE UNIQUE (b)-[r:Depends {version:"1.0.6"}]->(a);
MATCH (a {name:"scala-library", org:"org.scala-lang"}), (b {name:"company-services", org:"a.company"}) CREATE UNIQUE (b)-[r:Depends {version:"2.9.2"}]->(a);