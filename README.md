# SeeGreen 

Every year millions of dollars are wasted in improper waste management, which is an easy problem to tackle at the user level. By making an easy way for users to manage their waste properly, we can greatly reduce damage. We at SeaGreen wish to spread awareness about this topic and have created a unique, innovative solution utilizing computer vision, AI, and natural language processing.

## What it does
Our Android app comes with two features: 1) Recyclable item image recognition: Using the camera, users can query any object to learn how to sort it for waste. The categories are Trash, Recycling, and Compost.
2) AI Chatbot: This interactive chatbot lets users interact through text and natural language to learn the recycling status for anything they want to know about. The bot learns from machine learning and NLP to better respond to user questions.
How we built it
1) Item Recognition: A custom camera view utilizes Microsoft's Computer Vision API, IBM bluemix, and Android Studio, calling the APIs at regular intervals. In the back end, machine learning and NLP tell users which bin to place an object into using text analytics.
2) AI Chatbot: We mainly used Java on the front end, with Python Flask and IBM bluemix/text analytics in the back-end to process language. We had custom-tune some text analytics inside the Android application in order to classify API outputs for our purposes.

## Awards
### Best Environmental Hack at Earthack 2016

## What's next for SeeGreen
We plan to add the feature to recognize multiple objects at once and label each object's status in the image. 
It would also be great to gear the app towards children to encourage eco-friendly habits and environmental education.
We plan to improve the CV capabilities to more accurately recognize images. 
