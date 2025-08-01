package com.qithon.clearplate.domain.coupon.entity;

import com.qithon.clearplate.domain.coupon.dto.request.RegisterCouponRequest;
import com.qithon.clearplate.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

  @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @Column(length = 100, nullable = false)
  private String couponTitle;

  @Column(nullable = false, unique = true)
  private String couponCode;

  @Column(length = 100)
  private String couponDescription;

  private Integer couponDiscountValue; // 할인 금액

  @CreatedDate
  private LocalDateTime createdAt;    // 등록일

  private LocalDateTime expiresAt;    // 만료일

  private Boolean isUsed;

  @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Builder
  private Coupon(String couponTitle, String couponCode, String couponDescription,
      Integer couponDiscountValue, LocalDateTime expiresAt, Boolean isUsed, User user) {
    this.couponTitle = couponTitle;
    this.couponCode = couponCode;
    this.couponDescription = couponDescription;
    this.couponDiscountValue = couponDiscountValue;
    this.user = user;
    this.expiresAt = expiresAt;
    this.isUsed = isUsed;
  }

  /**
   * 쿠폰을 생성하는 정적 팩토리 메서드입니다.
   *
   * @param couponTitle         쿠폰 제목
   * @param couponDiscountValue 쿠폰 할인 금액
   * @param expiresAt           쿠폰 만료일
   * @param couponDescription   쿠폰 설명
   * @param user
   * @return 생성된 Coupon 객체
   */
  public static Coupon createCouponOf(String couponTitle, Integer couponDiscountValue,
      LocalDateTime expiresAt, String couponDescription, Boolean isUsed, User user) {
    return Coupon.builder()
        .couponTitle(couponTitle)
        .couponCode(generateCouponCode())
        .couponDescription(couponDescription)
        .couponDiscountValue(couponDiscountValue)
        .expiresAt(expiresAt)
        .user(user)
        .isUsed(isUsed)
        .build();
  }


  /**
   * 쿠폰 등록 요청 DTO를 인자로 받아 Coupon 객체를 생성합니다.
   * @param requestDTO 쿠폰 등록 요청 DTO 입니다.
   * @return 생성된 Coupon 객체
   */
  public static Coupon from(RegisterCouponRequest requestDTO) {
    return Coupon.builder()
        .couponTitle(requestDTO.getCouponTitle())
        .couponCode(generateCouponCode())
        .couponDescription(requestDTO.getCouponDescription())
        .couponDiscountValue(requestDTO.getCouponDiscountValue())
        .expiresAt(requestDTO.getExpiresAt())
        .isUsed(requestDTO.getIsUsed())
        .user(null)
        .build();
  }


  /**
   *  쿠폰 코드를 생성하는 메서드입니다
   * @return 16자리 랜덤 쿠폰 코드 생성
   */
  public static String generateCouponCode() {
      String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
      SecureRandom random = new SecureRandom();
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < 16; i++) {
        sb.append(characters.charAt(random.nextInt(characters.length())));
      }

      return sb.toString();
  }


}
