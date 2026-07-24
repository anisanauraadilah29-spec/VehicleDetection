# Rencana Pembaruan Splash Screen (Sesuai Referensi)

Mengubah tampilan Splash Screen agar sesuai dengan gambar referensi yang diberikan, dengan fokus pada tipografi modern, skema warna biru gelap, dan indikator pemuatan yang lebih bersih.

## User Review Required

> [!IMPORTANT]
> Saya tidak memiliki aset gambar latar belakang (kota/jalan) dan logo spesifik (perisai dengan mobil & motor) seperti di referensi. Untuk sementara, saya akan menggunakan **gradien warna** yang mirip dan tetap menggunakan logo yang sudah ada, namun dengan tata letak dan gaya teks yang persis seperti di gambar. Jika Anda memiliki file gambarnya, silakan masukkan ke folder `res/drawable`.

## Proposed Changes

### UI Layout

#### [MODIFY] [activity_splash.xml](file:///C:/Users/Anisa Naura Adilah/AndroidStudioProjects/VehicleDetection/app/src/main/res/layout/activity_splash.xml)
- **Background**: Menggunakan gradien biru gelap/navy yang dalam.
- **Typography**:
    - "VEHICLE": Putih, Bold, Italic.
    - "DETECTION": Biru Cyan, Bold, Italic.
    - Sub-teks: "Deteksi Kendaraan Cerdas" (Putih tipis).
- **Loading Area**:
    - Mengubah `ProgressBar` menjadi melingkar (Circular) berwarna Cyan.
    - Menambahkan teks "Memuat model..." di bawah ProgressBar.

### Aset Tambahan (Opsional/Placeholder)

#### [NEW] [bg_splash_gradient.xml](file:///C:/Users/Anisa Naura Adilah/AndroidStudioProjects/VehicleDetection/app/src/main/res/drawable/bg_splash_gradient.xml)
- Membuat gradien vertikal dari biru sangat gelap ke biru navy untuk latar belakang.

## Verification Plan

### Manual Verification
1. Jalankan aplikasi.
2. Pastikan teks "VEHICLE" dan "DETECTION" memiliki warna berbeda dan bergaya miring (italic).
3. Pastikan indikator loading berupa lingkaran dan terdapat teks "Memuat model..." di bawahnya.
4. Pastikan latar belakang terasa lebih dalam dan sesuai dengan estetika gambar referensi.
