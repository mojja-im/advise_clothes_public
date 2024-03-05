package com.advise_clothes.backend.controller;

import com.advise_clothes.backend.domain.entity.User;
import com.advise_clothes.backend.request.UserCreate;
import com.advise_clothes.backend.service.SessionService;
import com.advise_clothes.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    // session check하는 private method 만들기
    private final UserService userService;
    private final SessionService sessionService;

    /**
     * 유저 조회
     * 이후 휴대폰 번호나 이메일로 로그인 하게 할 건지 확인
     *
     * @return status 200 : 조회 성공 - 유저 있음
     * status 204 : 조회 성공 - 유저 없음
     */
    @GetMapping("")
    public ResponseEntity<User> login(@RequestParam(required = false) String account,
                                      @RequestParam(required = false) String password,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phoneNumber
    ) {
        User user = User.builder().account(account)
                .password(password)
                .email(email)
                .phoneNumber(phoneNumber).build();

        return password == null ? userService.findByUserForNotDelete(user)
                .map(value -> ResponseEntity.status(HttpStatus.OK).body(value))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new User()))
                : userService.findByUserForNotDelete(user, password)
                .map(value -> ResponseEntity.status(HttpStatus.OK).body(value))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new User()));
    }

    /**
     * 테스트용 코드
     *
     * @return body User
     */
    @GetMapping("/list")
    public List<User> userList() {
        return userService.findAll();
    }

    // TODO status 400 에러의 피드백 일치시키기
    // status 400에서 피드백하는 User form이 일치하지 않음
    // account만 가지고 있는 User, 아무 값도 가지고 있지 않는 User

    /**
     * 유저 생성
     *
     * @param userCreate 생성할 유저 정보
     * @return status 201 : 유저 생성 성공, body : 생성된 User
     * status 400 : account가 있으면 - 이미 있는 유저, body : account만 있는 User
     * account가 없으면 - 필수 정보를 입력하지 않음 (account, password, nickname, mail, phoneNumber), body : new User
     */
    @PostMapping("")
    public ResponseEntity<User> createUser(@RequestBody UserCreate userCreate) {
        if (!userCreate.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new User());
        }

        User user = userCreate.toEntity();

        return userService.findByUserForNotDelete(user)
                .map(value ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new User()))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user)));
    }

    /**
     * 유저 정보 변경
     * 수정 필요
     *
     * @param account 변경할 계정
     * @param user    변경할 값
     * @return status 200 : 변경 완료
     */
    @PutMapping("/{account}")
    public ResponseEntity<User> updateUser(@PathVariable String account, @RequestBody User user) {
        User userToFind = User.builder().account(account).build();
        return userService.findByUserForNotDelete(userToFind).map(value -> {
                    if (user.getPassword() != null) {
                        value.setPassword(user.getPassword());
                    }
                    if (user.getNickname() != null) {
                        value.setNickname(user.getNickname());
                    }
                    if (user.getEmail() != null) {
                        value.setEmail(user.getEmail());
                    }
                    if (user.getPhoneNumber() != null) {
                        value.setPhoneNumber(user.getPhoneNumber());
                    }       // 이후 휴대폰 본인인증으로 변경
                    if (user.getArea() != null) {
                        value.setArea(user.getArea());
                    }
                    if (user.getHeight() != null) {
                        value.setHeight(user.getHeight());
                    }
                    if (user.getWeight() != null) {
                        value.setWeight(user.getWeight());
                    }
                    return ResponseEntity.status(HttpStatus.OK).body(userService.update(value));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new User()));
    }

    // 테스트 아직 안 해봄
//    @PutMapping("/{session}")
//    public ResponseEntity<User> updateUser(@PathVariable Session session) {
//        return sessionService.findBySessionKey(session).isPresent()? userService.findByUserForNotDelete(session.getUser()).map(value -> {
//            if (session.getUser().getPassword() != null) { value.setPassword(session.getUser().getPassword()); }
//            if (session.getUser().getNickname() != null) { value.setNickname(session.getUser().getNickname()); }
//            if (session.getUser().getEmail() != null) { value.setEmail(session.getUser().getEmail()); }
//            if (session.getUser().getPhoneNumber() != null) { value.setPhoneNumber(session.getUser().getPhoneNumber()); }       // 이후 휴대폰 본인인증으로 변경
//            if (session.getUser().getArea() != null) { value.setArea(session.getUser().getArea()); }
//            if (session.getUser().getHeight() != null) { value.setHeight(session.getUser().getHeight()); }
//            if (session.getUser().getWeight() != null) { value.setWeight(session.getUser().getWeight()); }
//            return ResponseEntity.status(HttpStatus.OK).body(userService.update(value));
//        })
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new User())) :
//                ResponseEntity.status(HttpStatus.NO_CONTENT).body(new User());
//    }


    /**
     * 유저 탈퇴
     *
     * @param account 탈퇴할 계정
     * @return body User
     * status 200 : 탈퇴 성공
     * status 400 : 탈퇴 실패 - 계정을 찾을 수 없음
     */
    @DeleteMapping("/{account}")
    public ResponseEntity<User> deleteUser(@PathVariable String account) {
        User userToFind = User.builder().account(account).build();
        return userService.findByUserForNotDelete(userToFind).map(value -> {
            value.setDeletedReason(1);
            return ResponseEntity.status(HttpStatus.OK).body(userService.delete(value));
        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new User()));
    }

    /**
     * 임의로 만든 메서드. url로 간단하게 회원탈퇴를 복구할 수 있도록 했다.
     *
     * @param account
     * @return
     */
    @DeleteMapping("/{account}/reset")
    public ResponseEntity<User> resetDeleteUser(@PathVariable String account) {
        return userService.findByUser(User.builder().account(account).build()).map(value -> {
            value.setDeletedReason(0);
            return ResponseEntity.status(HttpStatus.OK).body(userService.update(value));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new User()));
    }
}
