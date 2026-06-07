# Demo ve Kullanım Kılavuzu

Bu kılavuz sistemi ilk kez çalıştıran bir kişinin kurulumu tamamlamasını, temel akışı göstermesini
ve teknik değerlendirme demosunu tekrarlanabilir biçimde sunmasını sağlar.

## 1. Demo öncesi kontrol listesi

- Docker çalışıyor.
- `8080`, `8081`, `5173` ve `5432` portları kullanılabilir.
- AI analizi gösterilecekse Ollama çalışıyor ve `gemma2:9b` modeli indirilmiş.
- API demo komutları kullanılacaksa `jq` kurulu.
- Repo kök dizinindesiniz.

Kontrol:

```bash
docker version
docker compose version
ollama list
```

Model yoksa:

```bash
ollama pull gemma2:9b
```

Ollama masaüstü uygulaması çalışmıyorsa:

```bash
ollama serve
```

## 2. Önerilen yöntem: tüm sistemi Docker ile çalıştırma

Repo kökünde:

```bash
docker compose -f docker-compose.full.yml up --build
```

İlk build bağımlılıkları indireceği için birkaç dakika sürebilir. Backend açılırken:

- PostgreSQL health kontrolünü bekler.
- Flyway migration'larını uygular.
- Mock servisten periyodik veri çekmeye başlar.

Ayrı terminalde servisleri doğrulayın:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl -I http://localhost:5173
```

Backend cevabı:

```json
{"status":"UP"}
```

Port çakışması varsa:

```bash
FRONTEND_PORT=15173 BACKEND_PORT=18080 MOCK_LAB_PORT=18081 \
docker compose -f docker-compose.full.yml up --build
```

Bu durumda frontend adresi `http://localhost:15173` olur.

## 3. Login ve doktor arayüzü

Tarayıcıda açın:

[http://localhost:5173](http://localhost:5173)

Demo hesabı:

```text
Kullanıcı adı: doctor
Şifre: Doctor123!
```

Beklenen akış:

1. Login sonrası hasta listesi açılır.
2. Mock servis her polling cycle'da yeni tüpler ürettiği için liste zamanla büyür.
3. Liste 10 saniyede bir otomatik yenilenir.
4. Kritik sonuçlar renk ve `Kritik` rozetiyle görünür.

## 4. Hasta arama ve filtre demo akışı

1. Hasta arama alanına küçük harfle `p-` yazın.
2. Yaklaşık 250 ms sonra eşleşen hasta numaraları öneri olarak görünür.
3. Bir öneriyi seçin.
4. Listenin hemen değişmediğini gösterin; sorgu ancak `Hastaları getir` butonuna basınca uygulanır.
5. `Gelişmiş filtreler` bölümünü açın.
6. Test kodu, durum veya tarih aralığı seçip `Filtreleri uygula` butonuna basın.
7. Bir hasta satırına tıklayarak detay sayfasına geçin.

Bu davranış, her tuş vuruşunda tüm listeyi sorgulamak yerine kontrollü bir UX sunar.

## 5. Hasta detay ve AI analizi

Hasta detayında her tüp ayrı kart olarak gösterilir. Tüp içindeki testler en ciddi durum önce olacak
şekilde sıralanır. Her testte:

- Değer ve birim
- Referans aralığı
- Görsel referans range bar
- Anormallik durumu

AI demosu:

1. Bir tüpte `AI analizi al` butonuna basın.
2. Butonun `Analiz ediliyor…` durumuna geçtiğini gösterin.
3. Başarılı cevapta özet, backend tarafından belirlenen işaretli testler, takip önerileri ve
   zorunlu disclaimer görünür.
4. Aynı tüpte tekrar istek yapılırsa `(sample, model, promptVersion)` cache'i kullanılır.

Ollama çalışmıyorsa uygulamanın geri kalanı çalışmaya devam eder ve AI paneli kontrollü hata gösterir.

## 6. Mock cihaz senaryolarını gösterme

Mock servisi doğrudan çağırarak bütün senaryolar görülebilir:

```bash
curl 'http://localhost:8081/api/device-results/batch?scenario=normal'
curl 'http://localhost:8081/api/device-results/batch?scenario=abnormal'
curl 'http://localhost:8081/api/device-results/batch?scenario=critical'
curl 'http://localhost:8081/api/device-results/batch?scenario=duplicate'
curl 'http://localhost:8081/api/device-results/batch?scenario=missing-field'
curl 'http://localhost:8081/api/device-results/batch?scenario=invalid-unit'
curl 'http://localhost:8081/api/device-results/batch?scenario=stale'
curl -i 'http://localhost:8081/api/device-results/batch?scenario=device-error'
```

Tekrarlanabilir random batch:

```bash
curl 'http://localhost:8081/api/device-results/batch?seed=42'
```

`device-error` için beklenen cevap `503 Service Unavailable` değeridir.

### Backend polling'i belirli senaryoya yönlendirme

Yerel backend'i zorunlu bir senaryo ile başlatmak:

```bash
cd backend-api
./mvnw spring-boot:run -Dspring-boot.run.arguments=--lab.polling.scenario=critical
```

Kullanılabilecek değerler:

```text
normal
abnormal
critical
duplicate
missing-field
invalid-unit
stale
device-error
```

Normal random akışa dönmek için backend'i scenario argümanı olmadan yeniden başlatın.

## 7. API'yi curl ile gösterme

JWT alın:

```bash
TOKEN=$(curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"doctor","password":"Doctor123!"}' | jq -r '.token')
```

Hasta listesi:

```bash
curl -s http://localhost:8080/api/patients \
  -H "Authorization: Bearer $TOKEN" | jq
```

Filtreli liste:

```bash
curl -s 'http://localhost:8080/api/patients?patientId=p-&status=CRITICAL&size=10' \
  -H "Authorization: Bearer $TOKEN" | jq
```

Audit log:

```bash
curl -s 'http://localhost:8080/api/audit-logs?size=10' \
  -H "Authorization: Bearer $TOKEN" | jq
```

AI analizi için önce bir `sampleId` alın:

```bash
PATIENT_ID=$(curl -s http://localhost:8080/api/patients \
  -H "Authorization: Bearer $TOKEN" | jq -r '.content[0].patientId')

SAMPLE_ID=$(curl -s "http://localhost:8080/api/patients/$PATIENT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.samples[0].sampleId')

curl -s -X POST "http://localhost:8080/api/samples/$SAMPLE_ID/ai-analysis" \
  -H "Authorization: Bearer $TOKEN" | jq
```

## 8. Swagger UI

Swagger UI:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

OpenAPI JSON:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Swagger dokümantasyonu public'tir; iş endpoint'leri JWT olmadan çağrıldığında `401` döner.

## 9. Yerel geliştirme yöntemi

Yalnızca PostgreSQL'i Docker'da başlatın:

```bash
docker compose up -d
```

Üç ayrı terminal kullanın.

Terminal 1:

```bash
cd mock-lab-service
./mvnw spring-boot:run
```

Terminal 2:

```bash
cd backend-api
./mvnw spring-boot:run
```

Terminal 3:

```bash
cd frontend
npm ci
npm run dev
```

Bu yöntem frontend ve backend değişikliklerinde daha hızlı geliştirme döngüsü sağlar.

## 10. Testleri çalıştırma

Backend:

```bash
cd backend-api
./mvnw test
```

Backend integration testleri Testcontainers kullandığı için Docker çalışmalıdır. Gerçek Ollama veya
mock servis gerekmez.

Mock servis:

```bash
cd mock-lab-service
./mvnw test
```

Frontend:

```bash
cd frontend
npm ci
npm test
npm run lint
npm run build
npm audit --audit-level=high
```

## 11. Veriyi sıfırlama ve sistemi durdurma

Full Docker stack:

```bash
docker compose -f docker-compose.full.yml down
```

Full stack verisini tamamen silmek:

```bash
docker compose -f docker-compose.full.yml down -v
```

Development PostgreSQL verisini tamamen silmek:

```bash
docker compose down -v
```

## 12. Sorun giderme

### AI analizi alınamıyor

Kontrol edin:

```bash
curl http://localhost:11434/api/tags
ollama list
```

`gemma2:9b` yoksa:

```bash
ollama pull gemma2:9b
```

Linux'ta full compose, `host.docker.internal:host-gateway` mapping'ini otomatik ekler.

### Frontend ilk açılışta geçici 502 gösteriyor

Backend Flyway migration'ları ve Spring context'i tamamlanmadan nginx proxy istek almış olabilir.
Şunu bekleyin:

```bash
curl http://localhost:8080/actuator/health
```

`{"status":"UP"}` geldikten sonra sayfayı yenileyin.

### Port zaten kullanılıyor

Full compose portlarını değiştirin:

```bash
FRONTEND_PORT=15173 BACKEND_PORT=18080 MOCK_LAB_PORT=18081 \
docker compose -f docker-compose.full.yml up --build
```

### Backend PostgreSQL'e bağlanamıyor

```bash
docker compose ps
docker compose logs postgres
```

Yerel geliştirmede backend'in `localhost:5432`, full compose içinde `postgres:5432` kullandığını
unutmayın.

### Testcontainers Docker bulamıyor

Docker'ın çalıştığını doğrulayın:

```bash
docker info
```

Ardından backend testini tekrar çalıştırın.

## 13. Önerilen sunum sırası

1. Mimarinin dört ana parçasını anlatın.
2. Mock serviste normal ve hata senaryolarını gösterin.
3. Login olup hasta arama/filtreleme UX'ini gösterin.
4. Kritik hasta ve tüp detayını açın.
5. AI analizinin kontrollü prompt, backend doğrulama, disclaimer ve cache kararlarını anlatın.
6. Audit log ve Swagger UI'ı gösterin.
7. Testleri, CI'ı ve full Docker çalıştırmayı gösterin.
8. Bilerek yapılmayan production özelliklerini ve nedenlerini açıklayın.
