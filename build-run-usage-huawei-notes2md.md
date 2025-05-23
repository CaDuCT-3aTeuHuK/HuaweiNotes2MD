# Сборка, запуск и порядок использования HuaweiNotes2MD
## Требования

Для работы сборки и запуска приложения HuaweiNotes2MD необходимо, чтобы были установлены следующие инструменты:
1. **Apache Maven версии 3.9.9** и выше
2. **Java 21**

## Сборка

Порядок сборки:
1. Скопировать корневую папку проекта к себе на компьютер.
2. Зайти в корневую папку проекта через терминал (например, Windows PowerShell).
3. В корневой папке проекта воспользоваться командой `mvn clean package`.
4. Дождитесь сборки проекта.

## Запуск

Для запуска проекта остаемся в терминале после сборки и выполняем нижеописанные действия:

Порядок запуска:
1. Перейти из корневой папки проекта в папку `target`.
2. Выполнить команду `java -jar huawei-notes-converter-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Порядок использования

Порядок использования приложения:
1. После запуска приложения в консоль будет выведено сообщение:

```
Выберите кодировку консоли:
1 = cp866
2 = UTF-8 
3 = windows-1251
```
> Выбор данной кодировки влияет на работу с кириллическими символами при считывании и сохранении по указанным путям конвертируемых файлов. Многое зависит от настроек определенного терминала и операционной системы. В моем случае для Windows PowerShell мною был выбран п.1. В случае, если кириллические символы при сохранении заменились на иероглифы стоит попробовать выбрать другую кодировку.

Допустим, необходимая кодировка cp866, следовательно вводим соответствующую ему цифру *1*.

2. Следующее сообщение, выводимое в консоль:

```
Введите путь к папке с данными Huawei:
```

Здесь необходимо ввести абсолютный путь к расположению ваших задач/заметок.  
Так, если вы хотите указать путь до задач, то необходимо распаковать скачанный архив из Облака Huawei, найти в нем папку *"To-dos"* и указать путь до нее. Если вы хотите указать путь до заметок, то в распакованном архиве нужно найти папку *"Notes"* и указать путь до нее.
Например, абсолютный путь до заметок в распакованном архиве будет выглядеть так:

```
d:\Notes-2025051603480271996\Notes\
```

3. После выбора пути к папке с данными Huawei будет выведено следующее сообщение:


```
Введите путь для сохранения Markdown-файлов:
```

Здесь необходимо ввести абсолютный путь к папке, в который вы хотите сохранить конвертированные Markdown-файлы.  
Например, абсолютный путь до такой паки может выглядеть так:

```
d:\Конвертированные Markdown-файлы\
```

4. После выбора пути для сохранения Markdown-файлов будет выведено следующее сообщение:


```
Конвертировать задачи (1) или заметки (2)?
```

У задач и заметок Huawei разный формат, в котором они хранятся. Для корректной обработки и сохранения их конвертированных версий необходимо указать соответствующий цифре тип конвертации файлов, которые хранятся по ранее указанному пути.  
Так для задач нужно ввести в консоль цифру *1*, а для заметок - *2*.

5. Результат конвертации хранится по указанному в п.3 пути. В зависимости от того, какие файлы вы конвертировали, задачи или заметки, они будут сохранены в соответствующие папки: *"Huawei Задачи"* или *"Huawei Заметки"*