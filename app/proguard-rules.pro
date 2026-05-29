# Room 混淆规则
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class edu.guigu.accountbook.data.model.** { *; }
-keep interface edu.guigu.accountbook.data.dao.** { *; }
