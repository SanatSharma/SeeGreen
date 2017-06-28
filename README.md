# SeeGreen

Every year millions of dollars are wasted in improper waste management, which is an easy problem to tackle at the user level. By making an easy way for users to manage their waste properly, we can greatly reduce damage. We at SeaGreen wish to spread awareness about this topic and have created a unique, innovative solution utilizing computer vision, AI, and natural language processing.

What it does
Our Android app comes with two features: 1) Recyclable item image recognition: Using the camera, users can query any object to learn how to sort it for waste. The categories are Trash, Recycling, and Compost.
2) AI Chatbot: This interactive chatbot lets users interact through text and natural language to learn the recycling status for anything they want to know about. The bot learns from machine learning and NLP to better respond to user questions.
How we built it
1) Item Recognition: A custom camera view utilizes Microsoft's Computer Vision API, IBM bluemix, and Android Studio, calling the APIs at regular intervals. In the back end, machine learning and NLP tell users which bin to place an object into using text analytics.
2) AI Chatbot: We mainly used Java on the front end, with Python Flask and IBM bluemix/text analytics in the back-end to process language. We had custom-tune some text analytics inside the Android application in order to classify API outputs for our purposes.

Challenges we ran into
Working with Android Studio to create a custom camera view was tricky. In addition, the CV portion required running asynchronous tasks with multiple threads, requiring very explicit synchronization for multi-programming. Python Flask was challenging to work with because servers didn't behave as efficiently as we thought to coordinate the different technologies we used. A lot of APIs had compatibility issues as well. IBM bluemix sometime yielded unexpected results.
Making sure that the chatbot knew the right questions to ask, responses to make and what input should prompt it took a lot of time. It was also tricky to code confirmation for answers that the user gives.

Accomplishments that we're proud of
We were able to create a cohesive product using more than five different APIs. It was very rewarding to be able to use a rising technology like computer vision to create an app that could make a big difference in many societies. This was many of our first experiences with Machine Learning and AI.

What's next for SeeGreen
We plan to add the feature to recognize multiple objects at once and label each object's status in the image. 
It would also be great to gear the app towards children to encourage eco-friendly habits and environmental education.
We plan to improve the CV capabilities to more accurately recognize images. 
