# تطبيق ذكر

مشروع Android باسم **ذكر** موجه لهواتف **Android 15** ويحتوي على:

- شاشة رئيسية فيها:
  - الذكر الصباحي
  - الذكر المسائي
  - إعداد وقت تنبيه صباحي
  - إعداد وقت تنبيه مسائي
  - أزرار لتفعيل أذونات الإشعارات والتنبيهات الدقيقة
- شاشة خاصة بالذكر الصباحي تحتوي:
  - الفاتحة
  - الإخلاص
  - الفلق
  - الناس
  - آية الكرسي
  - قريش
  - الشمس
- شاشة خاصة بالذكر المسائي تحتوي:
  - الفاتحة
  - الإخلاص
  - الفلق
  - الناس
  - آية الكرسي
  - قريش
  - الليل
- عند الضغط على **ابدأ الكل** يقرأ التطبيق العناصر السبعة تلقائيًا، وكل عنصر يتكرر **7 مرات** ثم ينتقل إلى العنصر الذي بعده.
- عند الضغط على **ابدأ من هنا** بجوار أي عنصر يبدأ من هذا العنصر ثم يكمل الباقي بالتتابع.
- تنبيهات صباحية ومسائية مع إعادة الجدولة بعد إعادة تشغيل الهاتف.

## التقنية

- Kotlin
- Jetpack Compose
- DataStore لحفظ أوقات التنبيه
- AlarmManager للتذكير
- TextToSpeech للقراءة الصوتية الحالية

## ملاحظة مهمة

القراءة الصوتية الحالية تعتمد على **TextToSpeech** الموجود في الهاتف. هذا مناسب كنموذج عمل أولي، لكن **لتلاوة قرآنية صحيحة** يفضل لاحقًا استبدال القراءة بمحرك تشغيل ملفات صوتية (`ExoPlayer`) مع ملفات MP3 موثقة لكل سورة/آية داخل `res/raw`.

## فتح المشروع

1. افتح المجلد في Android Studio.
2. دع Gradle يقوم بالمزامنة.
3. شغّل التطبيق على جهاز Android 15 أو محاكي API 35.
4. فعّل إذن الإشعارات من داخل التطبيق.
5. إن أردت دقة أعلى جدًا في وقت التنبيه، فعّل إذن التنبيهات الدقيقة من داخل التطبيق.

## أهم الملفات

- `app/src/main/java/com/zikr/app/MainActivity.kt`
- `app/src/main/java/com/zikr/app/ui/ZikrApp.kt`
- `app/src/main/java/com/zikr/app/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/zikr/app/ui/screens/DhikrScreen.kt`
- `app/src/main/java/com/zikr/app/playback/QuranTtsPlayer.kt`
- `app/src/main/java/com/zikr/app/notifications/ReminderScheduler.kt`
- `app/src/main/java/com/zikr/app/notifications/ReminderReceiver.kt`
- `app/src/main/java/com/zikr/app/data/QuranRepository.kt`
