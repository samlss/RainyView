# RainyView
[![Download](https://api.bintray.com/packages/samlss/maven/rainyview/images/download.svg)](https://bintray.com/samlss/maven/rainyview/_latestVersion)   [![Api reqeust](https://img.shields.io/badge/API-11+-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=11#l11)    [![Apache License 2.0](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/samlss/RainyView/blob/master/LICENSE)  [![Blog](https://img.shields.io/badge/samlss-blog-orange.svg)](https://blog.csdn.net/Samlss)

A rainy rainy rainy view. ( ˘•灬•˘ )

### [More](https://github.com/samlss/FunnyViews)

### [中文](https://github.com/samlss/RainyView/wiki/Chinese_Document)

<br/>


![gif1](https://github.com/samlss/RainyView/blob/master/screenshots/screenshot1.gif)

<br/>

![gif2](https://github.com/samlss/RainyView/blob/master/screenshots/screenshot2.gif)



------
### Usage

#### Gradle
Add it in your app build.gradle at the end of repositories:
  ```java
  dependencies {
      implementation 'me.samlss:rainyview:1.0.0'
  }
  ```

#### Maven
```java
<dependency>
  <groupId>me.samlss</groupId>
  <artifactId>rainyview</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

#### In layout.xml

```java
    <me.samlss.view.RainyView
          app:left_cloud_color="#B7AC8D"
          app:right_cloud_color="#9b8f84"
          app:raindrop_color="#9aa9bb"
          app:raindrop_creation_interval="10"
          app:raindrop_max_number="50"
          app:raindrop_max_length="50"
          app:raindrop_min_length="20"
          app:raindrop_min_speed="1"
          app:raindrop_max_speed="3"
          app:raindrop_size="15"
          app:raindrop_slope="-4"
          android:layout_weight="1"
          android:layout_width="0dp"
          android:layout_height="match_parent" />
```

#### In code
```java
rainyView.setLeftCloudColor(Color.parseColor("#B7AC8D")); //Set the color of the left cloud
rainyView.setRightCloudColor(Color.parseColor("#9b8f84")); //Set the color of the right cloud
rainyView.setRainDropColor(Color.parseColor("#9aa9bb")); //Set the color of the raindrop
rainyView.setRainDropMaxNumber(50); //Set the max number of the raindrop
rainyView.setRainDropMaxLength(50); //Set the max length of the raindrop
rainyView.setRainDropMinLength(20); //Set the min length of the raindrop
rainyView.setRainDropMaxSpeed(3); //Set the max speed of the raindrop
rainyView.setRainDropMinSpeed(1); //Set the min speed of the raindrop
rainyView.setRainDropSlope(-4); //Set the slope of the raindrop
rainyView.setRainDropCreationInterval(10); //Set the creation interval of the raindrop

rainyView.start(); //Start animation
rainyView.stop(); //Stop animation
rainyView.release(); //Release the rainy view
```


#### attr

```java
    <declare-styleable name="RainyView">
        <!--The color of raindrop-->
        <attr name="raindrop_color" format="color"></attr>

        <!--The color of the left cloud-->
        <attr name="left_cloud_color" format="color"></attr>

        <!--The color of the right cloud-->
        <attr name="right_cloud_color" format="color"></attr>

        <!--Number of raindrops that can coexist at the same time-->
        <attr name="raindrop_max_number" format="integer"></attr>

        <!--The creation of the raindrop interval in millis-->
        <attr name="raindrop_creation_interval" format="integer"></attr>

        <!--The min length in pixel of every raindrop-->
        <attr name="raindrop_min_length" format="integer"></attr>

        <!--The max length in pixel of every raindrop-->
        <attr name="raindrop_max_length" format="integer"></attr>

        <!--The size in pixel of every raindrop-->
        <attr name="raindrop_size" format="integer"></attr>

        <!--The min speed of every raindrop-->
        <attr name="raindrop_min_speed" format="float"></attr>

        <!--The max speed of every raindrop-->
        <attr name="raindrop_max_speed" format="float"></attr>

        <!--The slope of every raindrop-->
        <attr name="raindrop_slope" format="float"></attr>
    </declare-styleable>
```

### License

```
Copyright 2018 samlss

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
