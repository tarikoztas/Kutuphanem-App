# Kütüphanem — Kurulum ve Çalıştırma

Kişisel kitaplık uygulaması: kütüphane (raf) oluştur, kitapları barkod okutarak veya elle ekle,
okundu/okunmadı takibi yap. Veriler Firebase hesabında durur, başka cihazdan girince hepsi gelir.

- **Paket adı:** `com.tarik.kutuphanem`
- **Teknoloji:** Kotlin, Jetpack Compose (Material 3), MVVM + Clean Architecture, Hilt,
  Firebase Auth + Firestore, Retrofit, CameraX + ML Kit, Coil

---

## 1. Android Studio kurulumu

1. [developer.android.com/studio](https://developer.android.com/studio) adresinden Android Studio'yu indir ve kur.
   (İçinde JDK 17 ve Android SDK hazır gelir — bilgisayarda şu an sadece JDK 8 var, o yetmiyor.)
2. Android Studio'yu aç → **Open** → `D:\library-app` klasörünü seç.
3. İlk açılışta Gradle indirmesi birkaç dakika sürer, bekle.

## 2. Firebase ayarları

Firebase Console'da (mevcut projende):

1. **Authentication → Sign-in method → Email/Password → Enable**
2. **Firestore Database → Create database** → *Production mode* → bölge: `eur3` veya `europe-west3`
3. **Project settings → Your apps → Add app → Android**
   - Android package name: `com.tarik.kutuphanem`
   - İnen **`google-services.json`** dosyasını şuraya koy: `D:\library-app\app\google-services.json`
4. **Firestore → Rules** sekmesine bu depodaki `firestore.rules` dosyasının içeriğini yapıştır → **Publish**

> `google-services.json` gelmeden uygulama derlenmez. Bu dosya `.gitignore` içinde — paylaşma.

## 3. (İsteğe bağlı) Google Books API anahtarı

Türkçe kitaplarda arama kotasını yükseltir. Anahtarsız da çalışır.

1. [console.cloud.google.com](https://console.cloud.google.com) → Firebase ile **aynı proje**
2. **APIs & Services → Library → Books API → Enable**
3. **Credentials → Create credentials → API key** → anahtarı kopyala
4. `D:\library-app\local.properties` dosyasına şu satırı ekle:

```
GOOGLE_BOOKS_API_KEY=buraya_anahtarı_yapıştır
```

(`local.properties` Android Studio ilk sync'te kendisi oluşturur ve git'e girmez.)

## 4. Telefonda çalıştırma

1. Telefonda **Ayarlar → Telefon hakkında → Yapı numarası**na 7 kez dokun (geliştirici modu açılır).
2. **Ayarlar → Geliştirici seçenekleri → USB hata ayıklama**yı aç.
3. Telefonu USB ile bağla, "izin ver" de.
4. Android Studio'da üstteki cihaz listesinden telefonunu seç → yeşil **Run** düğmesine bas.

**APK üretmek için:** Build → Build Bundle(s)/APK(s) → Build APK(s).
Dosya: `app/build/outputs/apk/debug/app-debug.apk`

> Barkod taramayı gerçek telefonda test et; emülatörün kamerası barkod okumaz.

---

## Proje yapısı

```
app/src/main/java/com/tarik/kutuphanem/
├── core/          Ortak yardımcılar: AppResult, Türkçe arama/sıralama, ISBN, görsel sıkıştırma
├── domain/        Modeller (Kitap, Kutuphane, Kullanici) ve repository arayüzleri
├── data/          Firestore + Retrofit uygulamaları, DTO'lar, dönüştürücüler
├── di/            Hilt modülleri (Firebase, ağ, repository bağlamaları)
└── ui/            Compose ekranları: auth, libraries, books, detail, manual, scanner, search
```

## Veri modeli (Firestore)

```
users/{uid}                        fullName, email, createdAt
  libraries/{libraryId}            name, bookCount, createdAt
    books/{bookId}                 title, authors[], publisher, pageCount, isbn,
                                   description, coverUrl, hasLocalCover, isRead, addedAt
      media/cover                  base64   (yalnızca kamerayla çekilen kapaklar)
```

Kapak ayrı belgede tutuluyor: Firestore'un Android SDK'sı alan seçmeyi desteklemediği için,
kitap belgesine gömülen görsel her liste sorgusunda birlikte inerdi.

## Türkçe kitaplar için yapılanlar

- **ISBN-10 ↔ ISBN-13 çift sorgu** — eski baskılar kataloglarda ISBN-10 ile kayıtlı, barkod ISBN-13 basıyor
- **Serbest metin denemesi** — `isbn:` sorgusu boş dönerse düz arama tekrar deneniyor
- **Alan alan birleştirme** — başlık Google Books'tan, sayfa sayısı OpenLibrary'den gelebilir
- **Kamerayla kapak** — kitap hiçbir katalogda yoksa kapağı çek; sıkıştırılıp Firestore'a yazılır, diğer cihaza da gelir
- **İsimle arama** — barkod okunmuyorsa ad/yazar ile ara, sonuçtan seç
- **Yayınevi/yazar önerileri** — kendi geçmiş kayıtlarından otomatik tamamlama
- **Türkçe arama/sıralama** — "sisli" → Şişli, "cocuk" → Çocuk; I/ı sorunu çözülü, alfabetik sıra Ç/Ğ/İ/Ö/Ş/Ü'ye uygun
- **Bandrol filtresi** — 978/979 ile başlamayan barkodlar reddediliyor

## Testler

```
Android Studio → app/src/test → IsbnUtilsTest → sağ tık → Run
```

ISBN doğrulama, ISBN-10/13 dönüşümü ve barkod filtresi burada test ediliyor.
