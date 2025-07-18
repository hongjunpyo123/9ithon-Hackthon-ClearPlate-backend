package com.qithon.clearplate.domain.CLPrestaurant.service;

import com.qithon.clearplate.domain.CLPrestaurant.dto.request.CLPLocationVerifyRequest;
import com.qithon.clearplate.domain.CLPrestaurant.dto.request.CLPRestaurantRegisterRequest;
import com.qithon.clearplate.domain.CLPrestaurant.dto.response.CLPLocationVerifyResponse;
import com.qithon.clearplate.domain.CLPrestaurant.dto.response.CLPRestaurantRegisterResponse;
import com.qithon.clearplate.domain.CLPrestaurant.entity.CLPRestaurant;
import com.qithon.clearplate.domain.CLPrestaurant.repository.CLPRepository;
import com.qithon.clearplate.domain.CLPrestaurant.vo.Location;
import com.qithon.clearplate.domain.stamp.entity.Stamp;
import com.qithon.clearplate.domain.stamp.repository.StampRepository;
import com.qithon.clearplate.domain.user.entity.User;
import com.qithon.clearplate.domain.user.repository.UserRepository;
import com.qithon.clearplate.global.security.config.ServletLogin;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@ToString
@RequiredArgsConstructor
@Slf4j
public class CLPService {

  private final CLPRepository clpRepository;
  private final ServletLogin servletLogin;
  private final StampRepository stampRepository;
  private final UserRepository userRepository;

  public List<CLPRestaurantRegisterResponse> registerRestaurantList(
      List<CLPRestaurantRegisterRequest> requestDTOs) {

    clpRepository.saveAll(requestDTOs.stream()
        .map(requestDTO -> CLPRestaurant.from(requestDTO))
        .toList());
    return requestDTOs.stream()
        .map(requestDTO -> CLPRestaurantRegisterResponse.from(requestDTO))
        .toList();
  }

  public CLPRestaurant getRestaurantById(String restauranId) {
    return clpRepository.findByRestaurantId(restauranId)
        .orElseThrow(() -> new RuntimeException("레스토랑을 찾을 수 없습니다."));
  }

  public List<CLPRestaurant> getAllRestaurants() throws RuntimeException {
    List<CLPRestaurant> clpRestaurants = clpRepository.findAll();

    return clpRestaurants;
  }

  public List<CLPRestaurant> getAllRestaurantsByLocation(Location userlocation, Double distance) throws RuntimeException {
      List<CLPRestaurant> clpRestaurants = clpRepository.findAll();

      List<CLPRestaurant> filteredRestaurants = clpRestaurants.stream()
          .filter(restaurant -> Location
              .calculateDistanceInMeters(userlocation, Location.of(restaurant.getY(), restaurant.getX())) <= distance)
          .collect(Collectors.toList());

      return filteredRestaurants;
  }

  public List<CLPRestaurant> searchRestaurant(String keyword) throws RuntimeException {
    List<CLPRestaurant> clpRestaurants = clpRepository.findAll();

    List<CLPRestaurant> filteredRestaurants = clpRestaurants.stream()
        .filter(restaurant -> restaurant.getPlaceName().contains(keyword) || restaurant.getAddressName().contains(keyword))
        .collect(Collectors.toList());

    return filteredRestaurants;
  }


  public CLPLocationVerifyResponse verifyLocation(CLPLocationVerifyRequest request, HttpServletRequest httpServletRequest ) {
    if(clpRepository.findByRestaurantId(request.getRestaurantId()).isPresent()) {
      CLPRestaurant clpRestaurant = clpRepository.findByRestaurantId(request.getRestaurantId())
          .orElseThrow( () -> new RuntimeException("레스토랑을 찾을 수 없습니다."));
      log.info("레스토랑 정보: {}", clpRestaurant);

      Long userId = servletLogin.extractUserIdFromRefreshToken(httpServletRequest);
      log.info("유저 ID: {}", userId);
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));


      Stamp stamp = Stamp.builder()
          .user(user)
          .clpRestaurant(clpRestaurant)
          .createdAt(LocalDateTime.now())
          .build();
      stampRepository.save(stamp);


      //레스토랑 위치 객체
      Location restaurantLocation = Location.of(Double.parseDouble(clpRestaurant.getY()), Double.parseDouble(clpRestaurant.getX()));
      System.out.println("레스토랑 위치(기준) restaurantLocation = " + restaurantLocation.getLatitude() + ", " + restaurantLocation.getLongitude());

      //유저 위치 객체
      Location userLocation = Location.of(request.getUserLatitude(), request.getUserLongitude());
      System.out.println("유저 위치 userLocation = " + userLocation.getLatitude() + ", " + userLocation.getLongitude());


      //유저와 레스토랑 사이의 거리 계산
      Double distance =  Location.calculateDistanceInMeters(restaurantLocation, userLocation);
      System.out.println("(유저와 레스토랑의 거리) distance = " + distance + "m");
      if(distance <= 50) {
        CLPLocationVerifyResponse response = CLPLocationVerifyResponse.from(clpRestaurant);
        response.setDistance(String.valueOf(distance));
        return response;
      } else {
        throw new RuntimeException("레스토랑과 유저 사이의 거리가 50m를 초과합니다. : " + distance + "m");
      }

      //레스토랑과 유저 사이의 거리가 50m 이내인지 확인
    } else {
      throw new RuntimeException("레스토랑을 찾을 수 없습니다.");
    }
  }
}
