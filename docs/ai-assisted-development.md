# AI Destekli Geliştirme Yaklaşımı ve Benim Rolüm

Case yönergesi AI agent araçlarının kullanılabileceğini, ancak yazılan kodun anlaşılması ve kararların
savunulabilmesi gerektiğini belirtiyor. Bu projede Codex ve Claude Code araçlarından yararlandım.
Bu belge, araçların nerede yardımcı olduğunu ve mühendislik sahipliğini nasıl koruduğumu açıklar.

## AI Araçlarını Nerede Kullandım?

- Kod iskeletleri ve tekrar eden implementasyon işlerini hızlandırma
- Mevcut kodu tarama ve potansiyel riskleri listeleme
- Test senaryoları üretme ve eksik coverage alanlarını belirleme
- Frontend UX alternatifleri üzerinde çalışma
- Docker, CI ve dokümantasyon taslaklarını hızlandırma
- Refactor sırasında birbirini etkileyen dosyaları bulma

## Benim Sorumluluğum ve Sahipliğim

AI araçları karar verici olarak değil, geliştirme yardımcısı olarak kullanıldı. Benim rolüm:

- Case kapsamını ve öncelikleri belirlemek
- Domain'i tek testten hasta -> tüp -> panel modeline taşımak
- UX beklentilerini ve kabul kriterlerini belirlemek
- Mimari trade-off'ları seçmek
- Üretilen kodu okumak, anlamak ve mevcut yapıyla uyumunu kontrol etmek
- Test, gerçek Docker çalıştırma, API denemeleri ve manuel UI kontrolüyle sonuçları doğrulamak
- Gereksiz karmaşıklığı ve kapsam dışı özellikleri reddetmek
- Bulunan hataları nedenleriyle düzeltmek ve kararları dokümante etmek

## AI Çıktısını Neden Doğrudan Doğru Kabul Etmedim?

Geliştirme sürecinde test ve gerçek çalışma kontrolleri, ilk bakışta doğru görünen bazı sorunları
ortaya çıkardı:

- Ollama timeout'u kontrollü `AiAnalysisException` yerine ham exception olarak çıkıyordu. Timeout testi
  bunu yakaladı ve API'nin `503` davranışı düzeltildi.
- Geçersiz enum ve tarih query parametreleri `500` üretiyordu. Integration testleriyle `400
  ProblemDetail` davranışına çevrildi.
- Spring `PageImpl` doğrudan serialize edildiğinde response sözleşmesinin stabil olmadığı uyarısı
  oluşuyordu. Açık `PageResponse` DTO'su eklendi.
- Modelin döndürdüğü `flaggedTests` alanına güvenmek, listede olmayan testleri uydurmasına izin
  verebilirdi. Bu alan backend'in deterministic durumlarından üretilmeye başlandı.
- İlk Docker image tercihi Apple Silicon üzerinde build olmadı. Gerçek image build'iyle platform
  sorunu bulundu ve çoklu platform image seçildi.
- Vitest dependency'sindeki kritik güvenlik açığı `npm audit` ile bulundu ve patched sürüme yükseltildi.

Bu örnekler, AI ile hızlı kod üretmenin tek başına yeterli olmadığını; mühendislik değerinin doğru
kabul kriteri, doğrulama ve sahiplenmede olduğunu gösteriyor.

## Kodun Savunulabilirliğini Nasıl Korudum?

- Kritik iş kuralları küçük ve test edilebilir sınıflara ayrıldı.
- Dış servisler ince client katmanlarının arkasında tutuldu.
- Her mantıksal geliştirme ayrı conventional commit olarak kaydedildi.
- Önemli kararlar alternatifleri ve production farklarıyla dokümante edildi.
- Testler Ollama veya mock servis çalışmadan geçecek şekilde tasarlandı.
- Full-stack Docker akışı yalnızca yazılmadı; gerçek olarak build edilip login dahil doğrulandı.
- CI her push'ta backend, mock ve frontend kontrollerini tekrar çalıştırıyor.

## Sonuç

Codex ve Claude Code geliştirme hızını artırdı; ancak mimari yön, scope, UX kararları, kabul kriterleri,
doğrulama ve nihai sorumluluk bana aittir. Bu nedenle teslimde yalnızca çalışan kodu değil, aldığım
kararların nedenlerini, sınırlarını ve kanıtlarını da sunuyorum.

