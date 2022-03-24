package com.cos.security1.config.oauth;

import com.cos.security1.config.auth.PrincipalDetails;
import com.cos.security1.config.oauth.provider.FacebookUserInfo;
import com.cos.security1.config.oauth.provider.GoogleUserInfo;
import com.cos.security1.config.oauth.provider.NaverUserInfo;
import com.cos.security1.config.oauth.provider.OAuth2UserInfo;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    // 구글로부터 바은 userRequest 데이터에 대한 후처리는 loadUser 함수에서 진행된다.
    // loadUser는 구글로부터 회원 프로필 정보를 받아온다!
    // cf) 함수 종료 시 @AuthenticationPrincipal 어노테이션이 생성된다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // userRequest 객체에는 사용자 정보가 들어있다고 생각하면 된다.
        // userRequest.getClientRegistration() = ClientRegistration{registrationId='google', clientId='169291646771-olb05fpt9eb4bdv27ii1kbgnstb90sld.apps.googleusercontent.com', clientSecret='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', clientAuthenticationMethod=org.springframework.security.oauth2.core.ClientAuthenticationMethod@4fcef9d3, authorizationGrantType=org.springframework.security.oauth2.core.AuthorizationGrantType@5da5e9f3, redirectUri='{baseUrl}/{action}/oauth2/code/{registrationId}', scopes=[email, profile], providerDetails=org.springframework.security.oauth2.client.registration.ClientRegistration$ProviderDetails@1e2f1600, clientName='Google'}
        // 여기 있는 registrationId를 통해 어떤 oauth로 로그인했는지 확인이 가능하다.
        System.out.println("userRequest.getClientRegistration() = " + userRequest.getClientRegistration());

        // userRequest.getAccessToken() = org.springframework.security.oauth2.core.OAuth2AccessToken@c944b9ea
        System.out.println("userRequest.getAccessToken() = " + userRequest.getAccessToken());

        OAuth2User oAuth2User = super.loadUser(userRequest);

         /**
         * sub는 구글 로그인 아이디 정보, 이름, 프로필 이미지, 구글 이메일... 이런 정보가 들어간다.
         *
         * 우리는 위 정보를 바탕으로 우리 코드에 정보를 넣어줄 것이다.
         * 중복을 방지하기 위해서, username은 google_sub 형태로 (sub는 중복될 일이 없으니까)
         * password는 우리 서버만 아는 걸로, 암호화해서 넣을 예정이다.
         * 그리고 이메일 정보는 그대로 넘겨줄 것이다.
         */

        // <구글>
        // super.loadUser(userRequest).getAttributes() = {sub=107959218152023539623, name=지원, given_name=지원, picture=https://lh3.googleusercontent.com/a/AATXAJzlOwc3adxPWlBm9cNzi5PH5Dp6HgCiJKnpoqte=s96-c, email=ljwon77@gmail.com, email_verified=true, locale=ko}
        // System.out.println("super.loadUser(userRequest).getAttributes() = " + super.loadUser(userRequest).getAttributes());
        // <페이스북>
        // oAuth2User.getAttributes() = {id=2886222645010746, name=이지, email=ljwon77@daum.net}
        // <네이버>
        // oAuth2User.getAttributes() = {resultcode=00, message=success, response={id=uhVh9eHnCs_JH7YCWu-gNVDOGr91XkkX8ob1M29GUf0, email=ljwon77@hanmail.net, name=이지원}}
        System.out.println("oAuth2User.getAttributes() = " + oAuth2User.getAttributes());


        // 강제 회원가입 진행
        // 편의를 위해 클래스를 따로 만들어서 코드 수정
        OAuth2UserInfo oAuth2UserInfo = null;
        if(userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        }
        else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("페이스북 로그인 요청");
            oAuth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());

        }
        else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            // 네이버의 경우 json 형태로 넘어오고, response 안에 정보가 들어 있음.
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));

        }
        else {
            System.out.println("구글과 페이스북, 네이버만 지원합니다.");
        }

        // "google"
        String provider = oAuth2UserInfo.getProvider();
        // sub=107959218152023539623
        String providerId = oAuth2UserInfo.getProviderId();
        // ljwon77@gmail.com
        String email = oAuth2UserInfo.getEmail();
        // google_107959218152023539623
        String username = provider + "_" + providerId;
        // 임의로 생성해주기. 어차피 oauth2로 로그인한 사용자는 이 비밀번호를 알 필요가 없다.
        String password = bCryptPasswordEncoder.encode("메롱");
        // 역할 지정
        String role = "ROLE_USER";

        // 이미 회원가입 되어 있는 사용자 여부 판단
        User userEntity = userRepository.findByUsername(username);
        if(userEntity == null) {
            // 빌드 패턴
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            userRepository.save(userEntity);
        } else {
            System.out.println("이미 가입된 회원입니다.");
        }

        // 리턴 타입은 oauth2 타입이지만, 어차피 principalDetails가 상속받았음
        // 이렇게 만들게 되면 리턴한 값이 Authentication 객체 안에 다음처럼 들어가있게 된다.
        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
        //return super.loadUser(userRequest);

        /**
         * 우리가 구글 로그인 버튼을 클릭하면, 구글 로그인 창이 뜨고,
         * 프로필 클릭을 통해 로그인이 완료되면 code를 리턴받는다. (Oauth-Client 라이브러리)
         * 이 code를 통해서 accessToken을 요청하고. 이를 리턴 받는데
         * 여기까지가 userRequest에 대한 정보이다.
         *
         * 그리고, userRequest의 loadUser 함수를 통해서 회원 프로필 정보를 받게 되는 것이다.
         */
    }
}
