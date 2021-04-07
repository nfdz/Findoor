# Findoor
[![](https://jitpack.io/v/nfdz/findoor.svg)](https://jitpack.io/#nfdz/findoor)

Android Library for Indoor Wi-Fi Navigation.

<p align="center">
  <img src="dev/sample-scenario.png?raw=true" alt="Sample scenario"/>
</p>

## Roadmap
 * Improve similarity and algorithm.
 * Implement `FindoorCriteria` with different criteria and output parameters such as confidence, etc.

## Library

### Key points
The learning curve of this library is very fast, the classes and concepts with which you have to learn to work are the following:
 * [`Record`](https://github.com/nfdz/Findoor/blob/master/findoor/src/main/java/io/github/nfdz/findoor/model/Record.java) 
 * [`LocationComparison`](https://github.com/nfdz/Findoor/blob/master/findoor/src/main/java/io/github/nfdz/findoor/model/LocationComparison.java)
 * [`FindoorRecorder`](https://github.com/nfdz/Findoor/blob/master/findoor/src/main/java/io/github/nfdz/findoor/FindoorRecorder.java)
 * [`FindoorProcessor`](https://github.com/nfdz/Findoor/blob/master/findoor/src/main/java/io/github/nfdz/findoor/FindoorProcessor.java)

### Download: Jitpack

It is very use integrate this library in your project as a dependency of your build system thanks to Jitpack. If you use gradle, you just have to add the following in your 'build.gradle' file:

   ```gradle
   allprojects {
	 repositories {
	 ...
         maven { url 'https://jitpack.io' }
      }
   }
   ...
   dependencies {
      implementation 'com.github.nfdz:findoor:v1.0.1'
   }
   ```

Jitpack works with several build systems, please checkout the [documentation](https://jitpack.io/docs/BUILDING/) if you need help with yours.

## Sample app

This app shows several uses cases of this library and it is totally functional. Feel free to use it as you need. For example, you could use this app in order to get records of location spots you need and serve or embed them in your production app.

### Download

<p align="center"><a href="https://github.com/nfdz/findoor/releases">
  <img width="250" src="dev/githubBadge.png?raw=true" alt="Get it on Github"/>
</a></p>

### Screenshots

<p align="center">
  <img src="dev/screenshots/1.png?raw=true" width="250" alt="Main"/>
  <img src="dev/screenshots/2.png?raw=true" width="250" alt="Record"/>
  <img src="dev/screenshots/3.png?raw=true" width="250" alt="Compare"/>
</p>
<p align="center">
  <img src="dev/screenshots/4.png?raw=true" width="250" alt="List"/>
  <img src="dev/screenshots/5.png?raw=true" width="250" alt="Visualize"/>
  <img src="dev/screenshots/6.png?raw=true" width="250" alt="Try"/>
</p>

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

