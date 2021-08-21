## LitCovidGraphDatabase

A collection of Kotlin and Java components to load data from 
the NCBI LitCovid (https://www.ncbi.nlm.nih.gov/research/coronavirus/)
project into a Neo4j database. The LitCovid data repository 
aggregates data for over 163000 PubMed entries (Aug, 2021) 
relevant to the 2019 novel Coronavirus. This data source was 
selected because the basic PubMed attributes are annotated six 
(6) BioConcepts: Gene, Disease, Chemical, Mutation, Species, and 
CellLine. This annotation is performed by the PubTator data
mining process. In particular, Gene annotations are data mined
by the GNormPlus application.

The LitCovid data is made available for download in a file 
which adheres 
to the BioC XML format (http://bioc.sourceforge.net/). A Java
API (https://sourceforge.net/projects/bioc/files/BioC_Java_1.0.1.tar.gz/download)
is available to process BioC formatted file. A modified version
of the source code for this application is incorporated in this
repository. The original version of the Java application 
maps all the PubMed documents into a single in-memory
collection. The modified version included in this repository 
creates an in-memory representation for each document 
individually. This requires significantly less memory to 
load large data sources. 

Data for each PubMed document is loaded into a Neo4j 4.3.2 
Community Edition database. The nodes are: PubMedArticle, 
JournalIssue, Author, Annotation. There are two (2) types
of PubMedArticle nodes. The first type represents articles 
classified as relevant to Covid and incorporate a Covid label.
The second type represent PubMed articles referenced by 
the Covid articles and have a Reference label. It is possible
for a PubMedArticle node to have both Covid and Reference 
labels.

After data from the LitCovid BioC file has been loaded, it is 
supplemented by data from the COVID-19 Knowledge Graph Project
(https://ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/historical_releases.html
). Currently, only the metadata file available from this data 
source is used. This file has basic information for over 740000 
articles considered relevant to Covid. These data are used to 
fill in any gaps in the data loaded from the LitCovid data source.
At this time, no new PubMedArticle nodes are created from this
supplemental data source.

The majority of the codebase for this project is written in 
Kotlin 1.5 (https://kotlinlang.org/). Kotlin was chosen because
it supports most of the functional programming capabilities
associated with scala while retaining complete compatibility 
with Java.
