# Лаборатори №8 — Mockito mock объектууд

**F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)**

- **Оюутны нэр: Ц. Тодбилэг
- **Оюутны код: B232270045

## Ажлын орчин

```
$ java -version
openjdk version "21.0.12.1" 2026-08-18 LTS
OpenJDK Runtime Environment Microsoft-14941484 (build 21.0.12.1+1-LTS)
OpenJDK 64-Bit Server VM Microsoft-14941484 (build 21.0.12.1+1-LTS, mixed mode, sharing)
```

```
$ mvn -version
Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Maven home: C:\Program Files (x86)\apache\apache-maven-3.10.0
Java version: 21.0.12.1, vendor: Microsoft, runtime: C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot
Default locale: en_US, platform encoding: UTF-8, time zone: Asia/Ulaanbaatar
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

## Төслийн бүтэц

```
.gitignore
pom.xml
README.md
src/main/java/mn/edu/must/sqat/
    Student.java
    StudentRepository.java
    EmailSender.java
    CourseRegistrationService.java          # тестлэгдэх класс
src/test/java/mn/edu/must/sqat/
    CourseRegistrationServiceTest.java      # Хэсэг A-ийн тестүүд
results/
    mvn-test.txt                            # эцсийн ногоон гаралт
    mvn-test-mutant-1.txt                   # Хэсэг B, мутант 1
    mvn-test-mutant-2.txt                   # Хэсэг B, мутант 2
    mvn-test-mutant-3.txt                   # Хэсэг B, мутант 3
```

Тестийг ажиллуулах:

```
mvn test
```

## Хэсэг A — Тестүүд

**Тестийн тоо: 10** (7 энгийн тест + A8 параметржүүлсэн тестийн 3 мөр).
Энэ нь `results/mvn-test.txt` файлын сүүлийн мөртэй таарна:
`Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` ба `BUILD SUCCESS`.

`StudentRepository` ба `EmailSender`-ийг `@Mock` объектоор орлуулсан тул үйлчилгээг өгөгдлийн сан, SMTP сервергүйгээр тестэлсэн.

| № | Тестийн метод | Юуг шалгадаг | Mockito / JUnit хэрэгсэл |
|---|---|---|---|
| A1 | `register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail` | Өмнөх холбоо хичээлээ үзсэн идэвхтэй оюутан → `"OK"`, бүртгэл хадгалагдаж, и-мэйл илгээгдэнэ | `when/thenReturn`, `verify` |
| A2 | `register_unknownStudent_returnsErrorNoStudentAndSavesNothing` | Байхгүй оюутан → `"ERROR_NO_STUDENT"`, юу ч хадгалаагүй, и-мэйл яваагүй | `verify(..., never())` |
| A3 | `register_inactiveStudent_returnsErrorInactiveAndNoEmail` | Идэвхгүй оюутан → `"ERROR_INACTIVE_STUDENT"`, и-мэйл илгээгчтэй огт харьцаагүй | `verifyNoInteractions` |
| A4 | `register_missingPrerequisite_returnsErrorPrerequisitesAndSavesNothing` | Өмнөх холбоо хичээл дутуу → `"ERROR_PREREQUISITES"`, хадгалаагүй | `when` + `never()` |
| A5 | `register_emailSenderThrows_stillReturnsOkAndSaves` | И-мэйл илгээгч exception шидсэн ч `"OK"`, бүртгэл хадгалагдсан | `doThrow(...).when(...)` |
| A6 | `register_nullInput_throwsAndTouchesNothing` | `null` studentId **эсвэл** `null` courseId → `IllegalArgumentException`, хамаарлуудтай огт харьцаагүй | `assertThrows`, `verifyNoInteractions` |
| A7 | `register_success_emailBodyContainsCourseCode` | Илгээсэн и-мэйлийн биед хичээлийн код орсон | `ArgumentCaptor` |
| A8 | `register_invalidState_returnsMatchingError` | Гурван алдааны тохиолдол: оюутан байхгүй, оюутан идэвхгүй, хичээл байхгүй | `@ParameterizedTest` + `@CsvSource`, `lenient()` |

Нэг дуудалтад matcher хэрэглэсэн бол бүх аргументад matcher хэрэглэсэн, жишээ нь: `verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), body.capture())`.

### A8-д яагаад `lenient()` хэрэгтэй вэ

A8-ийн CSV мөр бүр `register()` доторх өөр өөр шалгалт дээр зогсдог: 1, 2-р мөрөнд метод `courseExists()`-ийг дуудахаас өмнө буцдаг тул тэр мөрүүдэд `courseExists`-ийн stub ашиглагдахгүй. `MockitoExtension` нь strict stubs горимтой бөгөөд ашиглагдаагүй stub-ийг алдаа (`UnnecessaryStubbingException`) гэж үздэг тул `lenient()` нь зөвхөн тэр нэг stub-ийг ашиглагдахгүй байхыг зөвшөөрч, бусад stub-ууд strict хэвээр үлдэнэ.

## Хэсэг B — Мини мутаци

Мутант бүр нь `CourseRegistrationService.java`-д хийсэн түр өөрчлөлт байсан. Ажиллуулалт бүрийн дараа кодыг эх хувилбарт нь сэргээж, эцэст нь ногоон гаралтыг `results/mvn-test.txt`-д дахин хадгалсан.

| Мутант | `CourseRegistrationService`-д хийсэн өөрчлөлт | Үр дүнгийн файл | Үр дүн | Унасан тестүүд |
|---|---|---|---|---|
| 1 | `if (!student.active()) return "ERROR_INACTIVE_STUDENT";` мөрийг комментолсон | `results/mvn-test-mutant-1.txt` | Failures: 2, Errors: 0 — BUILD FAILURE | **A3** `register_inactiveStudent_returnsErrorInactiveAndNoEmail` (хүлээгдсэн `ERROR_INACTIVE_STUDENT`, гарсан `ERROR_NO_COURSE`)<br>**A8 [2]** `register_invalidState_returnsMatchingError` (хүлээгдсэн `ERROR_INACTIVE_STUDENT`, гарсан `OK`) |
| 2 | `repository.saveRegistration(studentId, courseId);` дуудалтыг комментолсон | `results/mvn-test-mutant-2.txt` | Failures: 2, Errors: 0 — BUILD FAILURE | **A1** `register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail`<br>**A5** `register_emailSenderThrows_stillReturnsOkAndSaves`<br>(хоёулаа: *Wanted but not invoked: repository.saveRegistration*) |
| 3 | Эхний `if`-ээс `\|\| courseId == null` хэсгийг устгасан | `results/mvn-test-mutant-3.txt` | Failures: 1, Errors: 0 — BUILD FAILURE (A6-г зассаны дараа) | **A6** `register_nullInput_throwsAndTouchesNothing` (*Expected IllegalArgumentException to be thrown, but nothing was thrown*) |

### Мутант 3 — миний тестийн цоорхой

Анхны A6-тай үед мутант 3 **амьд үлдсэн**: 10 тест бүгд давсан. A6 зөвхөн `register(null, "CS101")`-г дууддаг байсан тул нөхцөлийн `courseId == null` хэсэг огт шалгагдаагүй. Мутанттай үед `register("S1", null)` exception шидэхгүй, харин repository-г дуудаж `"ERROR_NO_STUDENT"` буцаадаг.

**Засвар:** A6-д хоёр дахь шалгалтыг нэмсэн. Тестийн тоо 10 хэвээр байхын тулд нэг тест дотор нэмсэн:

```java
assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));
```

Энэ өөрчлөлтийн дараа A6 мутант 3 дээр унадаг болсон (`results/mvn-test-mutant-3.txt`), харин эх код дээр давдаг.

### Хэсэг B-ийн дүгнэлт

Одоо гурван мутант бүгд баригдсан бөгөөд тус бүрийг өөр төрлийн шалгалт барьсан: мутант 1-ийг буцаах алдааны код, мутант 2-ыг `saveRegistration` дээрх `verify`, мутант 3-ыг `assertThrows`. Мутант 2 нь `verify` яагаад хэрэгтэйг харуулж байна: метод `"OK"` буцаасаар байсан тул зөвхөн буцаах утгыг шалгадаг тест бүртгэл хадгалагдаагүйг анзаарахгүй байх байсан. Мутант 3 нь мөр ажилласан гэдэг нь тэр мөрийг тестэлсэн гэсэн үг биш гэдгийг харуулсан: A6 `if` мөрийг ажиллуулсан ч хоёр нөхцлийн зөвхөн нэгийг шалгадаг байсан тул тестээ өргөтгөх хүртэл эвдэрсэн нөхцөл илрээгүй.

## Дүгнэлт

Mock объектыг Mockito үүсгэдэг бөгөөд тест бүрт `when(...).thenReturn(...)`-ээр тусад нь програмчилдаг, мөн өөрт ирсэн дуудлагуудыг бүртгэдэг тул харилцааг `verify`-гоор шалгаж болдог. Гараар бичсэн fake бол интерфейсийн жижиг, ажилладаг хэрэгжүүлэлт, жишээ нь `HashMap` дээр суурилсан санах ойн `StudentRepository`; энэ нь өгөгдлийг үнэхээр хадгалж буцаадаг ч ямар метод дуудагдсаныг хэлж чадахгүй. Харилцаа өөрөө тестлэх гэж буй зан төлөв байх үед, жишээ нь "и-мэйл илгээгдсэн" эсвэл "юу ч хадгалаагүй" гэдгийг шалгахад mock илүү тохиромжтой, харин fake-ийг олон тестэд дахин ашиглахад хялбар бөгөөд код өөрчлөгдөхөд бага эвдэрдэг. Тестлэгдэж буй классыг, `Student` record шиг энгийн өгөгдлийн объектыг, мөн `List` шиг өөрсдийн бичээгүй төрлүүдийг mock хийх ёсгүй: эдгээрт бодит объект ашиглах нь илүү энгийн бөгөөд найдвартай. Энэ лабораторид зөвхөн хоёр гадаад хамаарал болох өгөгдлийн сан ба и-мэйл үйлчилгээг mock хийсэн, учир нь бодит тестэд яг эдгээр нь удаан ажиллах эсвэл гаж нөлөө үүсгэх байсан. `verify`-г хэтрүүлэн хэрэглэх нь эрсдэлтэй, учир нь тест код *юу* үр дүн гаргахыг биш *яаж* ажиллахыг шалгаж эхэлдэг тул `findStudent`-ийг хоёр удаа дуудах, дуудлагын дарааллыг солих зэрэг хор хөнөөлгүй refactoring зан төлөв зөв хэвээр байсан ч тестийг унагана. Тиймээс миний тестүүд зөвхөн чухал гаж нөлөөнүүдийг (`saveRegistration` ба `send`) `verify`-гоор шалгаж, бусад бүхнийг буцаах утгаар нь шалгадаг.
