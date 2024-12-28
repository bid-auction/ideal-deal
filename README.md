<div align="center"> 
<h1>찰떡딜</h1>
</div>
<p align="center">
  <img src="https://github.com/user-attachments/assets/d0d4ffcf-e918-4b7f-bb4f-46c990427674" width="150">
</p>

<p align="center">
  중고 상품을 좋아하는 이들이 자신의 중고 상품을 경매를 통해 거래하고, 경제적인 소비 생활을 할 수 있는
</p>

<p align="center">
  <strong>중고 거래 경매 플랫폼</strong>
</p>

<p align="center">
  입니다.
</p>
<br>

# 🔍 프로젝트 개요
- **기간** : 2024.11.18 ~ 2024.12.27
- **프로젝트 명** : 찰떡딜
- **팀원** : 홍민우, 박준영
- **워크 스페이스** : https://www.notion.so/15842bcefb368024b446ee640b473da6
<br>
  
# 📚 기술 스택
- **Java 17**
- **Spring Boot**
- **Spring Security**
- **Validation**
- **JWT**
- **OAuth 2.0**
- **JPA**
- **H2**
- **MySQL**
- **Postman**
- **Redis**
- **QueryDsl**
- **Scheduler**
- **Web Socket**
<br>

# 📱 와이어 프레임
<h2>회원가입 및 로그인</h2>
<img src="https://github.com/user-attachments/assets/e458d786-c525-4f51-9e5a-fe6ed86a1745" width="150">
<img src="https://github.com/user-attachments/assets/77c11915-05a7-48e1-805f-e18c5121c97f" width="150">
<br><br>

<h2>메인 화면 및 네비게이션 페이지</h2>
<img width="150" alt="메인 페이지@2x" src="https://github.com/user-attachments/assets/fe5f3688-0eb3-41ca-b702-e68f81bdb8bd" />
<img src="https://github.com/user-attachments/assets/cf6d252d-8eb1-45bb-a0a1-2d4930bbfaa1" width="150">
<img src="https://github.com/user-attachments/assets/eade2977-86ea-45d9-b647-39b1d0f39afa" width="150">
<img src="https://github.com/user-attachments/assets/14afda91-2762-4e10-a74d-67f36f371a1b" width="150">
<img src="https://github.com/user-attachments/assets/9a8ff2b3-6fcb-4c22-8445-f63d530730d9" width="150">
<br><br>
<img src="https://github.com/user-attachments/assets/c1e2bb81-ab9d-49a0-b1a2-a6da167e3399" width="150">
<img src="https://github.com/user-attachments/assets/84fbfbef-faa1-423f-9b94-5da94ac6b31b" width="150">
<br><br>

<h2>마이페이지</h2>
<img src="https://github.com/user-attachments/assets/80d80d18-aee4-47b5-9c7b-909dacaba387" width="150">
<img src="https://github.com/user-attachments/assets/b48d766a-c199-4726-ace5-772e09ba4d34" width="150">
<img src="https://github.com/user-attachments/assets/c1fb73fc-7b6f-40fb-a22d-c14c5d5f0e0b" width="150">
<img src="https://github.com/user-attachments/assets/57f0b9e5-e9f3-4ee7-8a0e-ed9f11211c07" width="150">
<img src="https://github.com/user-attachments/assets/2205de1e-dcde-4789-b48d-0fc532e8d170" width="150">
<br><br>

<h2>상품 조회페이지</h2>
<img src="https://github.com/user-attachments/assets/72b057bd-0b6d-497b-ac08-88528b154d0f" width="150">
<img src="https://github.com/user-attachments/assets/bfa598c9-eea4-4998-aec5-729f1dd09ef0" width="150">
<img src="https://github.com/user-attachments/assets/5f9eb093-cc76-4331-a256-d608efd9e0af" width="150">
<img src="https://github.com/user-attachments/assets/db880831-a1ff-4be9-82d0-02df28f54aee" width="150">
<img src="https://github.com/user-attachments/assets/d621d7d8-f61b-44c1-8321-d13bbc102248" width="150">
<br><br>
<img src="https://github.com/user-attachments/assets/1d061e53-0124-4b06-9aca-2dfa5d58252a" width="150">
<img src="https://github.com/user-attachments/assets/7c7c09a9-9425-4ed4-9120-2e67f882adaa" width="150">
<br><br>

# 📔 주요기능
<h2>🙇🏻‍♂️ 1. 회원 기능</h2>

- 회원가입 및 로그인
  - 사용자는 소셜 로그인을 통해 회원가입과 로그인을 간편하게 할 수 있습니다.
  - 또한, 사용자는 이메일로 회원가입을 할 수 있습니다.
  - 이메일로 회원가입을 진행 시에는, 해당 이메일에 발송된 코드를 인증해야만 회원가입이 가능합니다.
  - 회원가입 시, 비밀번호는 암호화되어서 저장도비니다.
  - 로그인 시, AccessToken과 RefreshToken이 발급됩니다.

- JWT
  - AccessToken : 짧은 유효기간을 가지며(1시간), 사용자 요청을 인증하는 데 사용됩니다.
  - RefreshToken : 긴 유효기간을 가지며(일주일), AccessToken이 만료되었을 때, 새로운 토큰을 발급받기 위해 사용됩니다.

- 충전
  - 회원은 입찰을 위한 돈을 충전할 수 있습니다.
 
- 경매 내역
  - 회원은 자신이 입찰한 경매 내역을 확인할 수 있습니다.
  - 상품에 대한 간단한 정보를 확인할 수 있습니다.
  - 경매가 성공했는지 실패했는지 확인할 수 있습니다.
  - 낙찰된 금액을 확인할 수 있습니다.
  - 경매 상세 내역을 조회할 수 있습니다.
 
- 경매 상세 내역
  - 판매자의 닉네임을 확인할 수 있습니다.
  - 상품에 대한 자세한 정보를 알 수 있습니다.
  - 낙찰된 금액을 조회할 수 있습니다.
  - 해당 상품에 대한 입찰 정보와, 본인이 입찰한 내역을 확인할 수 있습니다.
    
- 판매 내역
  - 회원은 자신이 판매한 판매 내역을 확인할 수 있습니다.
  - 상품에 대한 간단한 정보를 확인할 수 있습니다.
  - 판매가 성공했는지 실패했는지 확인할 수 있습니다.
  - 판매된 금액을 확인할 수 있습니다.
  - 판매 상세 내역을 조회할 수 있습니다.

- 판매 상세 내역
  - 낙찰자의 닉네임을 확인할 수 있습니다.
  - 상품에 대한 자세한 정보를 알 수 있습니다.
  - 판매된 금액을 조회할 수 있습니다.
  - 해당 상품에 대한 입찰 정보와, 본인이 입찰한 내역을 확인할 수 있습니다.
 
  
<br>    
<h2>📦 2. 상품 기능</h2>

- 상품 등록
  - 사용자는 로그인을 했을 경우, 상품을 등록할 수 있습니다.
  - 상품을 등록할 때 사진을 1개 이상 업로드 해야됩니다.
  - 상품을 등록할 때 제목, 상품 설명, 시작 경매일, 종료 경매일, 카테고리 선택은 필수입니다.
  - 상품을 등록할 때 시작은 현재시간 이후여야 합니다.
  - 상품이 등록되면, 서버에서 자동으로 시작 경매일과 종료 경매일에 맞춰 경매를 진행합니다.

- 상품 삭제
  - 회원은 상품을 삭제할 수 있습니다.
  - productId를 입력받아 해당 상품을 삭제합니다.
    
- 상품 수정
  - 회원은 상품을 수정할 수 있습니다.
  - productId를 입력받아 해당 상품을 수정합니다.
  - 상품을 수정할 때 사진을 1개 이상 업로드 해야됩니다.
  - 상품을 수정할 때 제목, 상품 설명, 시작 경매일, 종료 경매일, 카테고리 선택은 필수입니다.
  - 상품을 수정할 때 시작은 현재시간 이후여야 합니다.

- 상품 조회
  - 누구나 상품을 조회할 수 있습니다.
  - 메인 페이지에서 상품에 대한 간단한 정보를 확인할 수 있습니다.
  - 상품의 상세 정보를 조회할 수 있습니다.
  - 경매가 진행 전인 상품을 조회할 수 있습니다.
  - 경매가 진행 중인 상품을 조회할 수 있습니다.
  - 경매가 종료된 상품을 조회할 수 있습니다.
  - 일주일 간 낙찰된 금액 중 가장 높은 금액과 가장 낮은 금액을 조회할 수 있습니다.
  - 진행된 경매 중 입찰이 많이 이루어진 상품을 조회할 수 있습니다.

<br>
<h2>💸 3. 입찰 기능</h2>

- 경매 진행 전
  - 로그인을 하지 않으면 입찰은 불가능하지만 조회는 가능합니다.
  - 회원들은 경매가 시작되어야 입찰을 할 수 있습니다.
  - 몇 명의 회원이 상품을 조회하고 있는지 확인할 수 있습니다.
 
- 경매 진행 중
  - 서버에서 동적으로 경매가 시작됩니다.
  - 로그인을 하지 않으면 입찰은 불가능하지만 조회는 가능합니다.
  - 회원은 입찰 한계 금액을 등록하여 입찰 금액에 대한 한도를 설정할 수 있습니다.
  - 회원들은 입찰을 할 수 있습니다.
  - 상품 판매자는 입찰을 할 수 없습니다.
  - 입찰 금액은 경매 시작가 또는 현재 경매가보다 높아야 합니다.
  - 현재 잔액이 부족하면 입찰이 불가능합니다.
  - 현재 최고 입찰자가 본인일 경우, 추가 입찰은 불가능합니다.
  - 몇 명의 회원이 상품을 조회하고 있는지 확인할 수 있습니다.
 
- 경매 종료
  - 서버에서 동적으로 경매가 종료됩니다.
  - 경매 종료 시, 현재 연결된 웹소켓들이 종료됩니다.
  - 입찰에 성공한 입찰자는 금액을 지불하고, 낙찰이 결정됩니다.
  - 입찰에 실패한 입찰자들은 입찰 금액을 전액 반환받습니다.
  - 상품 판매자는 낙찰된 금액을 받게 됩니다.
  - 입찰 진행에 대한 정보는 저장됩니다.

<br>

# ⌛️ 경매 로직 흐름도

1. **상품 등록** :
- 유저(판매자)는 상품을 등록할 때, 원하는 **경매 시작일**과 **경매 종료일**을 설정합니다.
  
<br>

2. **스케줄러 등록** :
- 등록된 상품의 **경매 시작일**과 **경매 종료일** 정보를 **TaskScheduler**에 전달합니다.
- **TaskScheduler**는 해당 일정을 기반으로 자동으로 경매 시작 및 종료 작업을 예약합니다.
  
<br>

3. **멀티 쓰레드 대기 :**
- **TaskScheduler**는 3개의 쓰레드를 활용하여 상품의 **경매 시작일 및 종료일을** **관리**합니다.
- 각 쓰레드는 **경매 시작일**과 **종료일**을 지속적으로 모니터링하며 **대기 상태를 유지**합니다.
  
<br>

4. **경매 시작** :
- **경매 시작일**에 도달한 상품이 있을 경우, **TaskScheduler**는 **비동기**적으로 해당 상품의 경매를 시작합니다.
- 3개의 쓰레드를 **비동기적**으로 이용해 다수의 상품이 동일한 **경매 시작일**을 가지더라도 성능 저하 없이 **효율적으로 처리되도록 설계**되었습니다.

<div align="center"> 
  <img src="https://github.com/user-attachments/assets/eda8cdbd-2eae-4689-af3f-8c0e6dd55e38" width="50%" />
</div>

- 경매가 시작되면 해당 상품의 상태가 **TaskScheduler**에 의해 경매 상태가 **BEFORE**에서 **ONGOING**으로 변경됩니다.
- 경매 과정에서 입찰 요청이 집중될 경우, **DB 부하**를 줄이기 위해 입찰 정보를 관리하는 **Redis** 인스턴스가 생성됩니다.
  
<br>

5. **경매 중** :
- 경매 시작 시, 경매 전 대기 중이던 유저들에게 **경매가 시작되었다**는 문구를 전송합니다.
- 유저가 경매에 입장할 경우, 해당 경매에 참여 중인 유저 수를 나타내는 **ViewCount**가 +1 증가하며, 나갈 경우 -1 감소합니다.
- 경매 중인 상품의 실시간 입찰 상황은 **WebSocket**을 통해 클라이언트와 서버 간에 주고받습니다.
- 구매자가 입찰을 하게 되면 서버는 이를 검증 후 **WebSocket**으로 모든 참가자에게 **최신 입찰 정보를 전송**합니다.
- 입찰에 대한 모든 정보는 **Redis**에 저장되어 효율적으로 관리됩니다.
- 최고 입찰가가 변경될 때마다 실시간으로 반영됩니다.
  
<br>

6. **경매 종료** :
- 경매가 종료되면 해당 상품의 상태가 **TaskScheduler**에 의해 경매 상태가 **ONGOING**에서 **ENDED**로 변경됩니다.
- 경매 종료일에 도달한 상품은 **TaskScheduler**에 의해 **자동적이고 비동기적으로 종료**됩니다.
- **경매 종료 시**, 클라이언트에 경매가 종료되었다는 문구가 표시되며, 연결되어 있던 **WebSocket**이 해제됩니다.
- 종료된 경매의 **경매 정보**, **입찰 정보**, **판매 정보**는 **Redis**에서 조회한 후 데이터베이스(DB)에 저장됩니다. 이후, **Redis**에 저장된 해당 데이터는 **삭제**됩니다.
- **판매자**는 낙찰된 입찰 금액을 지급받고, **낙찰자**는 경매 금액을 지불하며 거래가 완료됩니다.
- **입찰에 실패한 사용자들**은 입찰에 사용한 금액 전액을 환불받습니다.
- 모든 금액 처리 로직은 **비동기적으로** 처리되기 때문에, 이 과정에서 데이터 **일관성**을 보장하기 위해 **@Lock**을 사용하여 **금액 반영 전 조회로 인한 오류를 방지**합니다.

<br><br>

# 📋 ERD

![image](https://github.com/user-attachments/assets/67c8cd66-63ef-4800-b092-59df3c86c88e)
