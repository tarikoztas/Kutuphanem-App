# Kütüphanem

Kişisel kitaplık ve barkod/ISBN takip uygulaması. Kütüphane (raf) oluştur, kitapları
barkod okutarak veya elle ekle, okundu/okunmadı durumunu takip et. Veriler Firebase
hesabında tutulduğu için başka bir cihazdan giriş yapınca tüm kitaplık senkron gelir.

## Teknolojiler

Kotlin · Jetpack Compose (Material 3) · MVVM + Clean Architecture · Hilt ·
Firebase Auth & Cloud Firestore · Retrofit + kotlinx.serialization · CameraX + ML Kit ·
Coil

## Özellikler

- E-posta/şifre ile kayıt ve giriş, cihazlar arası senkronizasyon
- Kütüphane (raf) oluşturma, yeniden adlandırma, silme
- Barkod/ISBN tarama (CameraX + ML Kit) — okuma alanı çerçeveyle sınırlı
- Google Books → OpenLibrary zinciriyle kitap bilgisi çekme, eksik alanları birleştirme
- İsimle arama ve sonuç önizlemesi
- Kitabı elle ekleme, kapağı kamerayla çekme
- Okundu/okunmadı rozeti, kütüphane içi anlık arama, ızgara/liste görünümü

### Türkçe kitaplara özel

- ISBN-10 ↔ ISBN-13 çift sorgu (eski baskılar kataloglarda ISBN-10 ile kayıtlı)
- `isbn:` sorgusu boş dönerse serbest metin denemesi — sonuç ISBN doğrulamasından geçer
- Katalogda olmayan kitaplar için kamerayla kapak çekme
- Türkçe'ye duyarlı arama ("sisli" → Şişli) ve alfabetik sıralama (Ç, Ğ, İ, Ö, Ş, Ü)
- Bandrol/fiyat barkodu filtresi (yalnızca 978/979 ile başlayan kodlar)

---

## Kurulum

Depoyu klonladıktan sonra **iki dosyayı kendin oluşturman gerekir** — ikisi de gizli
bilgi içerdiği için depoya dahil değildir.

### 1. Firebase

1. [Firebase Console](https://console.firebase.google.com)'da bir proje oluştur
2. **Authentication → Get started → Sign-in method → Email/Password → Enable**
3. **Firestore Database → Create database** (production mode)
4. **Project settings → Add app → Android**, paket adı: `com.tarik.kutuphanem`
5. İnen **`google-services.json`** dosyasını `app/` klasörüne koy
6. **Firestore → Rules** sekmesine bu depodaki [`firestore.rules`](firestore.rules)
   içeriğini yapıştır → Publish

### 2. local.properties

`local.properties.example` dosyasını `local.properties` adıyla kopyala ve doldur.
Android Studio projeyi ilk açtığında `sdk.dir` satırını kendisi yazar.

Google Books API anahtarı isteğe bağlıdır (anahtarsız da çalışır, kota daha düşüktür):
Google Cloud Console → **Books API**'yi etkinleştir → **Credentials → API key**.

> Anahtarı kısıtlarken **"Uygulama kısıtlamaları"nı "Yok" bırak.** "Android
> uygulamaları" kısıtlaması bu istemcide çalışmaz; Google `X-Android-Package` ve
> `X-Android-Cert` başlıklarını bekler, Retrofit bunları göndermez ve `403` alırsın.
> Güvenlik için **"API kısıtlamaları" → yalnızca Books API** seçilmesi yeterlidir.

### 3. Çalıştırma

Projeyi Android Studio'da aç, Gradle sync'i bekle, **Run**. Minimum Android 7.0 (API 24).

> Barkod taramayı gerçek cihazda test et; emülatörün sanal kamerası barkod okumaz.

---

## Güvenlik notu

Depoya girmeyen dosyalar: `local.properties`, `google-services.json`, imzalama
anahtarları (`*.jks`, `*.keystore`), derleme çıktıları (`build/`, `*.apk`).

Firestore kuralları her kullanıcıyı yalnızca kendi verisine kısıtlar:

```
match /users/{uid}/{document=**} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}
```

Firebase istemci API anahtarı zaten APK'nın içinde dağıtılır ve tek başına bir
yetki vermez — veriyi koruyan şey yukarıdaki kurallardır. Yine de gereksiz yere
paylaşmamak için `google-services.json` depo dışında tutulur.

## Proje yapısı

```
app/src/main/java/com/tarik/kutuphanem/
├── core/     Ortak yardımcılar: AppResult, Türkçe arama/sıralama, ISBN, görsel sıkıştırma
├── domain/   Modeller ve repository arayüzleri
├── data/     Firestore + Retrofit uygulamaları, DTO'lar, dönüştürücüler
├── di/       Hilt modülleri
└── ui/       Compose ekranları: auth, libraries, books, detail, manual, scanner, search
```

Ayrıntılı kurulum adımları için [KURULUM.md](KURULUM.md) dosyasına bak.
