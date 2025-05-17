package ru.spakhomov.huaweinotestomarkdown;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class HuaweiNotesToMarkdown {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Выберите кодировку консоли:\n1 = cp866\n2 = UTF-8\n3 = windows-1251");
        int charsetNameCode = scanner.nextInt();

        if (charsetNameCode == 1) {
            scanner = new Scanner(System.in, "Cp866");
        } else if (charsetNameCode == 2) {
            scanner = new Scanner(System.in, "UTF-8");
        } else if (charsetNameCode == 3) {
            scanner = new Scanner(System.in, "windows-1251");
        } else {
            System.err.println("Неверный выбор. Кодировка выставлена автоматически");
        }

        // 1. Запрос пути к папке с данными
        System.out.println("Введите путь к папке с данными Huawei:");
        String sourcePath = scanner.nextLine();

        // 2. Запрос пути для сохранения Markdown
        System.out.println("Введите путь для сохранения Markdown-файлов:");
        String outputPath = scanner.nextLine();

        // 3. Выбор типа данных
        System.out.println("Конвертировать задачи (1) или заметки (2)?");
        int choice = scanner.nextInt();

        Path outputDir;
        try {
            if (choice == 1) {
                // Обработка задач (оригинальная логика)
                outputDir = Paths.get(outputPath, "Huawei Задачи");
                Files.createDirectories(outputDir);

                Files.list(Paths.get(sourcePath))
                     .filter(Files::isDirectory)
                     .forEach(taskDir -> processTaskFolder(taskDir, outputDir));
                System.out.println("Конвертация задач завершена! Файлы сохранены в: " + outputDir);
            } else if (choice == 2) {
                // Обработка заметок (заготовка)
                outputDir = Paths.get(outputPath, "Huawei Заметки");
                Files.createDirectories(outputDir);
                processNoteFolders(sourcePath, outputDir);
                System.out.println("Конвертация заметок завершена! Файлы сохранены в: " + outputDir);
            } else {
                System.err.println("Неверный выбор. Допустимо: 1 (задачи) или 2 (заметки)");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файлов: " + e.getMessage());
        }
    }

    private static void processTaskFolder(Path taskDir, Path outputDir) {
        try {
            Path jsonFile = taskDir.resolve("json.js");
            if (!Files.exists(jsonFile)) {
                return;
            }

            // Чтение JSON
            String content = Files.readString(jsonFile);
            content = content.replace("var data = ", "");
            JSONObject json = new JSONObject(content);
            JSONObject task = json.getJSONObject("mTaskContent");

            // Получаем категорию (если нет поля, считаем 0)
            int categoryId = task.optInt("mCategoriesId", 0);

            // Определяем конечную папку для сохранения
            Path categoryDir = outputDir;
            if (categoryId != 0) {
                categoryDir = outputDir.resolve("Категория_" + categoryId);
                Files.createDirectories(categoryDir); // Создаём папку, если её нет
            }

            String body = task.optString("mBody", "").trim(); // Возвращает "" если поля нет
            if (body.isEmpty()) {
                body = "Без названия"; // Запасной вариант
            } else {
                // Формирование Markdown
                body = task.getString("mBody")
                                  .replace("\n", " ")  // Заменяем переносы строк на пробелы
                                  .replace("\r", " ")  // На всякий случай
                                  .trim();             // Удаляем лишние пробелы по краям
            }
            boolean isComplete = task.getInt("mComplete") == 1;
            long createTime = task.getLong("mOrdinaDate");
            long modifyTime = task.getLong("mModifiedTime");
            long reminderTime = task.getLong("mReminderTime");
            long reminderType = task.getLong("mReminderType");

            // Формируем имя файла (первые 15 символов без запрещённых символов)
            String baseName = (isComplete ? "ВЫПОЛНЕНО " : "") +
                              body.substring(0, Math.min(body.length(), 15))  // Обрезаем, но не превышаем длину
                                  .replaceAll("[\\\\/:*?\"<>|]", "_");    // Заменяем запрещённые символы

            String fileName = generateUniqueFileName(categoryDir, baseName, ".md");

            // Конвертация времени (Москва)
            ZoneId moscowZone = ZoneId.of("Europe/Moscow");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String createDate = Instant.ofEpochMilli(createTime)
                                       .atZone(moscowZone)
                                       .format(formatter);

            String modifyDate = Instant.ofEpochMilli(modifyTime)
                                       .atZone(moscowZone)
                                       .format(formatter);

            String reminderDate = null;
            if (reminderType == 1) {
                reminderDate = Instant.ofEpochMilli(reminderTime)
                                      .atZone(moscowZone)
                                      .format(formatter);
            }

            // Формирование Markdown-контента
            String markdown = String.format(
                    "---\n" +
                    "Время создания: %s\n" +
                    "Время изменения: %s\n" +
                    "%s" +
                    "tags:\n" +
                    "  - %s\n" +
                    "---\n\n" +
                    "%s",  // Здесь тело заметки сохраняется "как есть" с переносами
                    createDate, modifyDate, reminderType == 1 ? ("Напоминание: " + reminderDate + "\n") : "",
                    isComplete ? "выполненнаязадача" : "невыполненнаязадача",
                    task.getString("mBody")  // Оригинальный текст с \n
            );

            // Сохранение файла
            Path outputFile = categoryDir.resolve(fileName);
            Files.writeString(outputFile, markdown, StandardOpenOption.CREATE);

        } catch (Exception e) {
            System.err.println("Ошибка при обработке папки " + taskDir + ": " + e.getMessage());
        }
    }

    private static void processNoteFolders(String sourcePath, Path baseOutputDir) throws IOException {
        {
            Files.list(Paths.get(sourcePath))
                 .filter(Files::isDirectory)
                 .forEach(noteDir -> {
                     try {
                         Path jsonFile = noteDir.resolve("json.js");
                         if (!Files.exists(jsonFile)) {
                             return;
                         }

                         // Чтение JSON
                         String content = Files.readString(jsonFile);
                         content = content.replace("var data = ", "");
                         JSONObject note = new JSONObject(content);
                         JSONObject noteContent = note.optJSONObject("content");

                         // Получаем tag_id из корневого объекта
                         String tagId = noteContent.optString("tag_id", "no_category");
                         Path categoryDir = baseOutputDir.resolve("Категория_" + tagId);
                         Files.createDirectories(categoryDir);

                         if (noteContent == null) {
                             return;
                         }

                         // Извлечение данных
                         String rawContent = noteContent.optString("content", "");
                         String title = extractTitle(noteContent, rawContent);
                         String body = convertContentToReadable(rawContent);
                         long created = noteContent.optLong("created", 0);
                         long modified = noteContent.optLong("modified", 0);

                         // Формирование Markdown
                         String markdown = String.format(
                                 "---\n" +
                                 "Время создания: %s\n" +
                                 "Время изменения: %s\n" +
                                 "---\n\n" +
                                 "%s",
                                 formatTime(created),
                                 formatTime(modified),
                                 body
                         );

                         // Генерация имени файла
                         String fileName = generateUniqueFileName(categoryDir,
                                 title.replaceAll("[\\\\/:*?\"<>|]", "_"), ".md");

                         // Сохранение
                         Files.writeString(categoryDir.resolve(fileName), markdown,
                                 StandardOpenOption.CREATE);

                     } catch (Exception e) {
                         System.err.println("Ошибка обработки заметки в " + noteDir + ": " + e.getMessage());
                     }
                 });
        }
    }

    private static String extractTitle(JSONObject noteContent, String rawContent) {
        // 1. Проверяем data1
        String title = noteContent.optString("data1", "").trim();
        if (!title.isEmpty()) {
            return title;
        }

        // 2. Ищем первый Text элемент в content
        String[] elements = rawContent.split("<>><><<<");
        for (String element : elements) {
            if (element.startsWith("Text|")) {
                String textContent = element.substring(5).trim();
                // Берем первую строку до переноса строки
                String firstLine = textContent.split("\\R")[0].trim();
                if (!firstLine.isEmpty()) {
                    return firstLine;
                }
            }
        }

        // 3. Используем поле title как последнее средство
        return noteContent.optString("title", "Без названия").trim();
    }

    private static String convertContentToReadable(String content) {
        // Разделяем на элементы
        String[] elements = content.split("<>><><<<");

        StringBuilder result = new StringBuilder();

        for (String element : elements) {
            if (element.startsWith("Text|")) {
                result.append(element.substring(5)).append("\n\n");
            }
            else if (element.startsWith("Bullet|")) {
                String bulletContent = element.substring(7);
                // Обрабатываем маркеры выполнения (0/1 в начале)
                if (bulletContent.matches("^[01].*")) {
                    String status = bulletContent.charAt(0) == '1' ? "- [x]" : "- [ ]";
                    result.append(status).append(" ").append(bulletContent.substring(1)).append("\n");
                } else {
                    result.append("- ").append(bulletContent).append("\n");
                }
            }
            else if (element.startsWith("Checkbox|")) {
                result.append("- [ ] ").append(element.substring(9)).append("\n");
            }
            else if (element.startsWith("CheckboxChecked|")) {
                result.append("- [x] ").append(element.substring(16)).append("\n");
            }
        }

        return result.toString().trim();
    }

    private static String formatTime(long timestamp) {
        if (timestamp == 0) return "Не указано";

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                                    ZoneId.of("Europe/Moscow"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String generateUniqueFileName(Path dir, String baseName, String extension) {
        String fileName = baseName + extension;
        int counter = 1;

        // Проверяем существование файла и добавляем (1), (2) и т.д.
        while (Files.exists(dir.resolve(fileName))) {
            fileName = baseName + " (" + counter + ")" + extension;
            counter++;
        }
        return fileName;
    }
}
