# Лабораторная работа №4. Взаимодействие с сервером.
Код приложения написан на языке Java и использует Android SDK.

## Инструкция по использованию приложения
Данное приложение сохраняет статистику проигрываемых песен на радио Мегабайт. Для сохранения песни и названия была создана база данных, содержащая
таблицу со следующими полями:
1. ID
2. Исполнитель
3. Название трека
4. Время внесения записи
```java
        String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_SONGS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ARTIST + " TEXT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
```
При включении приложения производится проверка подключения к Интернету. В случае если подключение отсутствует – выводится всплывающее сообщение `Toast` с предупреждением о запуске в автономном режиме (доступен только просмотр внесенных ранее записей).
<p align="center">
<img src="https://sun9-75.userapi.com/s/v1/ig2/KgGDiavYhE6_W_RDLK9YIsxzssqxJV8JKIUJ6YLQf2xg1mF2cwI5lebSCZ8YyaYDTZDjNI4ND_NvCPg6LRhlykrW.jpg?quality=95&as=32x68,48x101,72x152,108x228,160x338,240x507,360x760,480x1013,540x1140,640x1351,720x1520&from=bu&u=yX4SfP33-YjVWTeIrAdEEwyAAbVPVq7H5lcVjU9V7nM&cs=720x1520" width="250" height="500"> 
</p>

После включения приложение производит асинхронный опрос сервера с интервалом 20 секунд. Если название трека не совпадает с последней записью в таблице, то производится запись в БД. URL адрес, по которому можно получить информацию о текущем треке и исполнителе: http://media.ifmo.ru/api_get_current_song.php. Формат возвращаемых данных – JSON. В случае успешного выполнения запроса результат будет иметь вид: `{“result”: “success”, “info” : “Исполнитель – Название трека” }`. В случае ошибки API вернет следующую строку: `{“result”: “error”, “info” : “Информация об ошибке” }`. Для успешного взаимодействия с API при обращении к странице передается логин и пароль как POSTпараметры.
login: `4707login`
password: `4707pass`
```java
private void fetchCurrentSong() {
        new Thread(() -> {
            try {
                URL url = new URL("http://media.ifmo.ru/api_get_current_song.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "login=4707login&password=4707pass";
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("result").equals("success")) {
                    String info = jsonResponse.getString("info");
                    String[] parts = info.split(" – ");
                    String artist = parts[0];
                    String title = parts[1];

                    if (!lastSong.equals(title)) {
                        dbHelper.addSong(artist, title);
                        lastSong = title;
                        activity.runOnUiThread(activity::displaySongs);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in MyTask", e);
            }
        }).start();
    }
```
