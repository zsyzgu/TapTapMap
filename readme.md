### Usage

将collect包加入到项目中，在存活能力强的进程中创建并调用Trigger。

目前支持两种Trigger：
1. ClickTrigger用于录制单次数据，可在检测到taptap后调用。

```java
clickTrigger = new ClickTrigger(getApplicationContext(), Trigger.CollectorType.All);
clickTrigger.trigger(); // trigger之后录制当前数据
```

2. TimerTrigger用于定时保存数据，开启即可。


```java
timerTrigger = new TimerTrigger(getApplicationContext(), Trigger.CollectorType.All);
timerTrigger.trigger(); // trigger之后开始每十分钟录一次数据
```

Trigger的第二项参数用于规定录制的数据类型，分以下几种：

1. CompleteIMU，高采样率的IMU数据

2. SampledIMU，低采样率的IMU数据

3. NonIMU，非IMU的传感器数据，包含气压计、光照、屏幕亮度

4. Location，高德地图返回的位置数据（[sdk](https://lbs.amap.com/api/android-sdk/summary)）

5. Weather，高德地图返回的天气数据

6. Bluetooth，蓝牙数据

7. Wifi，无线网数据

8. All，所有以上数据

可传入List来规定录制其中的部分数据。