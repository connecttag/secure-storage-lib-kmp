# ConnectTag Secure Storage Library (secure-storage-lib-kmp)

[![JitPack](https://jitpack.io/v/connecttag/secure-storage-lib-kmp.svg)](https://jitpack.io/#connecttag/secure-storage-lib-kmp)

مكتبة تخزين بيانات الاعتماد والرموز السرية بأمان عبر المنصات (Kotlin Multiplatform) مع تشفير عتادي قوي على أندرويد عبر Android Keystore وAES/GCM وتوفير عقود تجريدية موحدة عبر المنصات:
- **Android** (API 24+)
- **Desktop JVM** (Windows, macOS, Linux)
- **iOS** (Arm64, SimulatorArm64)

---

## 📦 التثبيت والإضافة للمشروع (Installation via JitPack)

### 1. إضافة مستودع JitPack في `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. إضافة التبعية في `build.gradle.kts`:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // For Kotlin Multiplatform:
            implementation("com.github.connecttag.secure-storage-lib-kmp:secure-storage-lib-kmp:1.0.0")
        }
    }
}
```

الحزمة الأساسية للمكتبة: `org.connecttag.lib.securestorage.*`

---

## 🌟 الميزات الأساسية

1. **`PlatformSecureStringVault` (عقد التخزين الآمن الموحد):**
   - واجهة برمجية موحدة لقراءة وكتابة وحذف النصوص السرية وتفريغها بناءً على بادئة المفاتيح (Key Prefix Namespacing).
2. **`AndroidKeystoreSecureStringVault`:**
   - تطبيق عالي الأمان لنظام Android يعتمد على خزانة المفاتيح العتادية (Hardware-backed AndroidKeyStore) وتشفير متقدم `AES/GCM/NoPadding` مع مفاتيح فريدة ومحمية.
3. **`TokenVault` & `StringVaultTokenVault`:**
   - إدارة متخصصة لحفظ وتحديث وقراءة رموز الوصول والتجديد (`AccessToken` و `RefreshToken`) بشكل ذري وآمن دون تسريب مفاتيح النظام.
4. **`CredentialStore` & `StringVaultCredentialStore`:**
   - متجر منظم للمفاتيح والبيانات السرية يعزل المساحات التسمية (Namespaces) ويمنع تداخل مفاتيح الحسابات المختلفة.
5. **`InMemorySecureStringVault`:**
   - خزانة في الذاكرة لتسهيل الاختبارات الأحادية والتطوير السريع وبيئات العرض المؤقت.
