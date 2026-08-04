import java.util.Scanner

abstract class Akun(
    val nim: String,
    private var password: String,
    protected var _saldo: Double = 100000.0
) {
    fun getSaldo(): Double = _saldo

    fun cekPassword(inputPassword: String): Boolean = password == inputPassword

    open fun tambahSaldo(jumlah: Double) {
        _saldo += jumlah
    }

    open fun kurangiSaldo(jumlah: Double): Boolean {
        return if (jumlah > _saldo) {
            false
        } else {
            _saldo -= jumlah
            true
        }
    }

    abstract fun tampilkanInfo(): String
}

class Nasabah(
    nim: String,
    password: String,
    val nama: String,
    saldo: Double = 100000.0
) : Akun(nim, password, saldo) {

    override fun tampilkanInfo(): String {
        return "NIM: $nim | Nama: $nama | Saldo: Rp${"%,.2f".format(getSaldo())}"
    }
}

interface Transaksi {
    fun tarikTunai(nasabah: Nasabah, jumlah: Double): Boolean
    fun setorTunai(nasabah: Nasabah, jumlah: Double): Boolean
    fun transfer(pengirim: Nasabah, nimTujuan: String, jumlah: Double): Boolean
    fun lihatSaldo(nasabah: Nasabah): Double
}

class Bank(private val namaBank: String) : Transaksi {

    private val daftarNasabah: MutableMap<String, Nasabah> = mutableMapOf()

    fun tambahNasabah(nasabah: Nasabah) {
        daftarNasabah[nasabah.nim] = nasabah
    }

    fun cariNasabah(nim: String): Nasabah? = daftarNasabah[nim]

    fun login(nim: String, password: String): Nasabah? {
        val nasabah = daftarNasabah[nim]
        return if (nasabah != null && nasabah.cekPassword(password)) nasabah else null
    }

    override fun tarikTunai(nasabah: Nasabah, jumlah: Double): Boolean {
        return nasabah.kurangiSaldo(jumlah)
    }

    override fun setorTunai(nasabah: Nasabah, jumlah: Double): Boolean {
        nasabah.tambahSaldo(jumlah)
        return true
    }

    override fun transfer(pengirim: Nasabah, nimTujuan: String, jumlah: Double): Boolean {
        val penerima = daftarNasabah[nimTujuan] ?: return false
        if (pengirim.nim == nimTujuan) return false
        if (!pengirim.kurangiSaldo(jumlah)) return false
        penerima.tambahSaldo(jumlah)
        return true
    }

    override fun lihatSaldo(nasabah: Nasabah): Double = nasabah.getSaldo()

    fun getNamaBank(): String = namaBank
}

fun main() {
    val scanner = Scanner(System.`in`)
    val bank = Bank("Rafi's Mini Bank")

    bank.tambahNasabah(Nasabah("130426", "05062006", "Rafi Athallah Ahmad Haryanto"))
    bank.tambahNasabah(Nasabah("128606", "29112006", "Keisha Aria Lai"))
    bank.tambahNasabah(Nasabah("130562", "04062006", "Muhammad Faathin Naufal"))

    println("=====================================")
    println("       WELCOME TO ${bank.getNamaBank().uppercase()}")
    println("=====================================")

    // ---------- LOGIN ----------
    println("\n[from Admin to Tester] Untuk NIM dan Password ada di readme ya!! :>\n")
    print("Masukkan NIM        : ")
    val nim = scanner.nextLine().trim()
    print("Masukkan Password (tanggal lahir, format ddMMyyyy): ")
    val password = scanner.nextLine().trim()

    val nasabah = bank.login(nim, password)

    if (nasabah == null) {
        println("\nLogin GAGAL. NIM atau password salah.")
        return
    }

    println("\nLogin berhasil! Selamat datang, ${nasabah.nama}.")

    var lanjut = true
    while (lanjut) {
        println("\n------ MENU TRANSAKSI ------")
        println("1. Transfer")
        println("2. Tarik Tunai")
        println("3. Setor Tunai")
        println("4. Lihat Saldo")
        println("5. Keluar")
        print("Pilih menu (1-5): ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                print("Masukkan NIM tujuan  : ")
                val tujuan = scanner.nextLine().trim()
                print("Masukkan jumlah transfer: ")
                val jumlah = scanner.nextLine().trim().toDoubleOrNull() ?: 0.0

                val berhasil = bank.transfer(nasabah, tujuan, jumlah)
                if (berhasil) {
                    println("Transfer sebesar Rp${"%,.2f".format(jumlah)} ke $tujuan BERHASIL.")
                } else {
                    println("Transfer GAGAL. Periksa NIM tujuan atau saldo tidak mencukupi.")
                }
            }
            "2" -> {
                print("Masukkan jumlah tarik tunai: ")
                val jumlah = scanner.nextLine().trim().toDoubleOrNull() ?: 0.0
                val berhasil = bank.tarikTunai(nasabah, jumlah)
                if (berhasil) {
                    println("Tarik tunai Rp${"%,.2f".format(jumlah)} BERHASIL.")
                } else {
                    println("Tarik tunai GAGAL. Saldo tidak mencukupi.")
                }
            }
            "3" -> {
                print("Masukkan jumlah setor tunai: ")
                val jumlah = scanner.nextLine().trim().toDoubleOrNull() ?: 0.0
                bank.setorTunai(nasabah, jumlah)
                println("Setor tunai Rp${"%,.2f".format(jumlah)} BERHASIL.")
            }
            "4" -> {
                println(nasabah.tampilkanInfo())
            }
            "5" -> {
                lanjut = false
                println("Terima kasih telah menggunakan ${bank.getNamaBank()}.")
            }
            else -> println("Pilihan tidak valid, silakan coba lagi.")
        }
    }
}
