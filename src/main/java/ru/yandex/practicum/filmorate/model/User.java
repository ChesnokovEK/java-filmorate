package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private int id;
    @Email(message = "Введите корректный адрес электронной почты")
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
