# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.tarik.kutuphanem.**$$serializer { *; }
-keepclassmembers class com.tarik.kutuphanem.** {
    *** Companion;
}
-keepclasseswithmembers class com.tarik.kutuphanem.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Firestore, dokumanlari yansima (reflection) ile modele donusturur
-keepclassmembers class com.tarik.kutuphanem.data.remote.dto.** {
    <init>();
    <fields>;
}

# Retrofit
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault
-dontwarn okhttp3.**
-dontwarn okio.**
