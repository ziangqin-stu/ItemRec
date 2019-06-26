# ItemRec
an item recommender system based on personalized user data and user location

## Development Plan
### System Sketch
![image](https://github.com/ziangqin-stu/ItemRec/blob/master/pic/%E7%B3%BB%E7%BB%9F%E8%AE%BE%E8%AE%A1%E7%AE%80%E5%9B%BE.png?raw=true)
### Function Lines
1. **User “Click” History Analysis**
   1. **Frontend “Onclick” Function**: collect user click behavior
   1. **(Kafka) Message Queue**: create independent message module & handle high-concurrency
   1. **Online Analysis Module (OAM)**: store and retrieve user click history in real-time and in LBS style
      1. Online DB: store and offer user click history
      1. Online Data Retrival:  retrive user click history based on location

2. **User “Like” History Analysis**
   1. <del>**Frontend JS Function**</del>: collect user “like” behavior
   1. <del>**DataBase**</del>: store and offer user behavior history
   1. <del>**Personal History**</del>: retrive all categories contained in user “like” history
   1. **Beam**: manupulating data from DB to feed ML algorithm in Rec-Engine
   1. **Rec-Engine**: train recommendation model to support "Group Based" recommendation
   1. **Merge Logic-1**: merge recommended categories from Rec-Engine and Data base by weight
   1. **Merge Logic-2**: select item based on merged recommended categories; merge items from Rec-Engine, Data and OAM by weight; prepare the final recommendation list for frontend

3. **Frontend Supplementaries**
   1. Login Page
   1. Clickable Linkpages
   1. Frontend Frameworks
   1. ...
   
