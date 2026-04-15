# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Apache POI classes
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Keep XML related classes
-keep class org.w3c.dom.** { *; }
-keep class javax.xml.** { *; }
