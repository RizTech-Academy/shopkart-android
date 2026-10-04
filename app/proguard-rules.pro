# Retrofit interfaces are referenced reflectively.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# kotlinx.serialization keeps its generated serializers on the companion.
-keepclassmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if class ** { kotlinx.serialization.KSerializer serializer(...); }
-keep class <1>$Companion { *; }
