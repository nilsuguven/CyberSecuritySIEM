# 🛡️ Cybersecurity SIEM (Security Information and Event Management) System

Bu proje, nesne yönelimli programlama (OOP), tasarım desenleri (Design Patterns) ve veri yapıları kullanılarak geliştirilmiş modüler bir **Siber Güvenlik Olay Yönetimi (SIEM) Kural Motorudur**.

Sistem, ham log verilerini güvenlik filtrelerinden geçirerek olası tehditleri tespit etmeyi, güvenlik olaylarını tehdit seviyelerine göre önceliklendirmeyi, kritik olaylar için bildirim üretmeyi ve olay kayıtlarını kalıcı olarak saklamayı amaçlar.

---

## 🏗️ Mimari ve Tasarım Desenleri

### 1. Chain of Responsibility (Sorumluluk Zinciri)

**Amaç:** Gelen log kayıtlarını birbirinden bağımsız güvenlik filtrelerinden geçirmek.

**Uygulama:** `AbstractLogFilter` sınıfından türeyen `BruteForceFilter` ve `IpBlacklistFilter` sınıfları kullanılır.

- **BruteForceFilter:** Belirlenen zaman aralığında aynı IP adresinden gelen başarısız giriş denemelerini takip eder. Eşik değerine ulaşıldığında kritik seviyeli bir güvenlik olayı oluşturur.
- **IpBlacklistFilter:** Kara listede bulunan IP adresleriyle ilişkili aktiviteleri kontrol ederek yüksek öncelikli olay oluşturur.

Filtrelerin sırası ve birden fazla kuralın eşleşmesi durumunda uygulanacak öncelik mantığı, olayların doğru sınıflandırılmasını sağlamak amacıyla belirlenmiştir.

### 2. Observer Pattern (Gözlemci Deseni)

**Amaç:** Güvenlik olayları oluştuğunda ilgili analistlere bildirim gönderilmesini sağlamak.

**Uygulama:** `AlertPublisher`, olay bildirimlerini abonelere ileten bileşendir. `AnalystNotifier` ise bildirimleri alan gözlemci olarak görev yapar.

### 3. Veri Kalıcılığı (Data Persistence)

**Amaç:** Uygulama yeniden başlatıldığında güvenlik olaylarının ve durum bilgilerinin korunması.

**Uygulama:** `SIEMEngine` ile `FileRepository` arasındaki iş birliği sayesinde olaylar dosyaya kaydedilir ve yeniden yüklenir. Aktif ve çözümlenmiş olaylar, durumlarına uygun biçimde yönetilir.

---

## 📊 Veri Yapıları

- **PriorityQueue:** Güvenlik olaylarını tehdit skorlarına göre önceliklendirmek için kullanılır. Kuyruğun doğru sıralanması, kullanılan karşılaştırma kuralına bağlıdır.
- **HashMap ve List:** Brute force tespitinde IP adreslerine ait başarısız giriş zamanlarını takip etmek ve belirlenen zaman penceresindeki denemeleri değerlendirmek için kullanılır.

---

## 📐 UML Sınıf Diyagramı

Projenin sınıflarını ve aralarındaki ilişkileri gösteren PlantUML kaynak dosyası `UML_Diagram.puml` içerisinde bulunmaktadır. Diyagram, sistemin mimarisini ve bileşenler arasındaki temel ilişkileri açıklamak amacıyla hazırlanmıştır.