package kitchenpos.menu.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kitchenpos.dao.MenuGroupDao;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.dto.MenuGroupRequest;
import kitchenpos.menu.dto.MenuGroupResponse;
import kitchenpos.product.dto.ProductResponse;
import kitchenpos.utils.IntegrationTest;

/**
 * @author : byungkyu
 * @date : 2021/01/19
 * @description :
 **/
@DisplayName("메뉴그룹")
class MenuGroupServiceTest extends IntegrationTest {

	@Autowired
	private MenuGroupService menuGroupService;

	@DisplayName("메뉴 그룹을 등록할 수 있다.")
	@Test
	void create() {
		// given
		MenuGroupRequest menuGroupRequest = new MenuGroupRequest("한식");

		// when
		MenuGroupResponse savedMenuGroupResponse = menuGroupService.create(menuGroupRequest);

		// then
		assertThat(savedMenuGroupResponse.getId()).isNotNull();
		assertThat(savedMenuGroupResponse.getName()).isEqualTo(menuGroupRequest.getName());
	}

	@DisplayName("메뉴 그룹의 목록을 조회할 수 있다.")
	@Test
	void list() {
		// given
		MenuGroupRequest menuGroupRequest = new MenuGroupRequest("한식");
		MenuGroupResponse menuGroupResponse = menuGroupService.create(menuGroupRequest);

		// when
		List<MenuGroupResponse> menuGroups = menuGroupService.list();
		List<Long> productNames = menuGroups.stream()
			.map(menuGroup -> menuGroup.getId())
			.collect(Collectors.toList());

		//then
		assertThat(productNames).isNotEmpty();
		assertThat(productNames).contains(menuGroupResponse.getId());
	}
}