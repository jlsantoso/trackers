# Trackers

The trackers or the so-called collecting agents are independent components that collect user data from services that expose the data via REST services.

Trackers fetch the data from a service and push it to another LRS (check our example here: https://github.com/svencharleer/datastore). Therefore, they are responsible to handle communication issues with providers and consumers (of data).

Trackers are JAVA Google App Engine applications: https://cloud.google.com/appengine/docs 

In fact, they are just normal web applications. Servlets that we execute from time to time.

We provide three examples of how to collect data from three services:

* Blogs
* Twitter: https://twitter.com/
* ARLearn: http://portal.ou.nl/web/arlearn

We summarize what a tracker does in seven steps:

1. fetch information about the services related to the course,,
2. iterate over these different services,
3. fetch the user data (the result is usually an array of user actions),
4. iterate over this array of user actions,
5. transform every user action to simplied xAPI,
6. enrich this data with some specic information such as the course identier,
7. push the event to the LRS.

## Related documents/publications/deliverables

* Journal of Universal Computer Science (http://www.jucs.org/): Under review
* weSPOT project (D5.2): http://wespot.net/public-deliverables

## I wanna re-use this project, what do I need to know?

### Cron file

Trackers are scheduled tasks. Google App engine enables to schedule tasks very easy. 

Where? https://github.com/jlsantoso/trackers/tree/master/StepUpTrackersV2/war/WEB-INF

Edit the cron file: cron.xml

E.g.: 

	<cron>
		  <url>/cron/addarlearn</url>
		  <description>Update ARLearn data</description>
		  <schedule>every 5 minutes</schedule>
	</cron>
	
<url> -> Is the url-pattern defined in your xml, because... yes, a way to schedule task is scheduling the execution of servlets.

NOTE 1: Btw, if you don't remember where you define your servlets... check the web.xml in the same folder where we have the cron.xml file! ;)

NOTE 2: If you want to disable a cron job, use this: <!-- --> In the end, it is a normal xml... 

### So... servlets, sorry... I mean trackers

#### ARLearn Tracker
Src: https://github.com/jlsantoso/trackers/blob/master/StepUpTrackersV2/src/org/be/kuleuven/hci/stepup/servlets/AddARLearn.java

Three important functions:
* getARLearnRuns: 
  - Do you remember Step 1? Gather data related to the course - In our case, it it the wespot platform: http://inquiry.wespot.net 
  - ARLearn works with runs. Every run is associated to an inquiry. Therefore, we need to request this relation to the platform.
  - Notice that you need a key "{insert_key}" to make it work. Please contact the project if you are interested on using weSPOT and ARLearn.
* getUserDataFromARLearnActions and getUserDataFromARLearnResponses (in charge of the other 7 steps)
  - ARLearn exposes the data with services: actions and reponses. Actions are events related to start something. Responses is the actual content.
  - Both have the same logic and do the rest of steps.
  - Notice that you need another key when you request user information to ARLearn - this line RestClient.doGetAuth. Same than before contact the project.

#### Blog Tracker
src: https://github.com/jlsantoso/trackers/blob/master/StepUpTrackersV2/src/org/be/kuleuven/hci/stepup/servlets/AddRSSServlet.java
* Notice that we use rome: http://rometools.github.io/rome/
* First things first, configure the rome properties file https://github.com/jlsantoso/trackers/blob/master/StepUpTrackersV2/src/rome.properties
* What is relevant to know? 
  - ReadGoogleSpreadSheetThesis14: is in charge of reading a Google spreadsheet where we have all the urls of the blogs. This is an example for a specific course.
  - ReadingBlogs: contains the different functions to collect blog information. We have two possibilities:
    + RSS
    + wordpress API: we access via API instead of RSS because they provide a richer dataset
* The process:
  - We generate an ArrayList with all the blogs related to the course (see urlsBlogs variable).
  - We access to the blog post and the comments feeds separately.
  - We add the context (i.e.: the course)
  - We push the data.


#### Twitter Tracker
src: https://github.com/jlsantoso/trackers/blob/master/StepUpTrackersV2/src/org/be/kuleuven/hci/stepup/servlets/AddTweetServlet.java
* Notice that we use twitter4j: http://twitter4j.org/en/index.html
* First things first, configure the twiter properties file https://github.com/jlsantoso/trackers/blob/master/StepUpTrackersV2/src/twitter4j.properties
* What is relevant to know? 
  - ReadGoogleSpreadSheetChikul13: is in charge of reading a Google spreadsheet where we have all the twitter handles related to the course. This is an example for a specific course.
  - It's important to notice that we process the hashtag of the course and the twitter handles of the students separately. We realised that twitter did not index all the tweets when querying by hashtag. Nevetheless, we wanted to track interaction from externals (i.e: querying the hashtag) and the students tweets querying their timelines and filtering those tweets related to the course.
* The process:
  - We generate an ArrayList with all the blogs related to the course (see urlsBlogs variable).
  - We access to timelines and the hashtag.
  - We add the context (i.e. the hashtag)
  - We push the data.

#### Update false service
