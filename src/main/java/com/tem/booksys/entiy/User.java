/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tem.booksys.entiy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Data
public class User {
    @NotNull
    private Integer id;
    private String username;
    @JsonIgnore //添加这个注解，当SpringMvc将该对象转为Json字符串时，忽略掉这个属性，最终转出的Json中就不含有这个属性
    private String password;
    //在接口处使用@Validated启动验证
    @NotEmpty
//    @Pattern(regexp = "^\\S{1,10}$")
    private String nickname;

    @NotEmpty
    @Email
    private String email;

    private String userPic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    //用户类型 1.管理员 2.普通用户
    private Integer type;
    //是否可以进行借阅 1:可以  2否
    private Integer state;
//    private  Integer isBorrow;
    private String msg;


}
