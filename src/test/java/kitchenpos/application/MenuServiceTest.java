package kitchenpos.application;

import static java.util.Optional.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;

/**
 * @author : byungkyu
 * @date : 2021/01/19
 * @description :
 **/
@DisplayName("메뉴")
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
	@Mock
	MenuDao menuDao;
	@Mock
	MenuGroupDao menuGroupDao;
	@Mock
	MenuProductDao menuProductDao;
	@Mock
	ProductDao productDao;

	MenuGroup 두마리메뉴 = mock(MenuGroup.class);
	MenuGroup 한마리메뉴 = mock(MenuGroup.class);

	Product 후라이드 = mock(Product.class);

	@DisplayName("메뉴를 등록할 수 있다.")
	@Test
	void create() {
		// given
		Long 두마리메뉴_id = 1L;
		Long 한마리메뉴_id = 2L;
		when(두마리메뉴.getId()).thenReturn(두마리메뉴_id);
		when(한마리메뉴.getId()).thenReturn(한마리메뉴_id);

		Long 후라이드_id = 1L;
		when(후라이드.getId()).thenReturn(후라이드_id);
		when(후라이드.getName()).thenReturn("후라이드");
		when(후라이드.getPrice()).thenReturn(BigDecimal.valueOf(16000));

		Menu 후라이드치킨 = new Menu("후라이드치킨", BigDecimal.valueOf(16000), 한마리메뉴.getId());
		MenuProduct 후라이드치킨_수량 = new MenuProduct(후라이드치킨.getId(), 후라이드_id, 1L);
		후라이드치킨.addMenuProduct(후라이드치킨_수량);

		when(menuGroupDao.existsById(후라이드치킨.getMenuGroupId())).thenReturn(true);
		when(productDao.findById(후라이드_id)).thenReturn(ofNullable(후라이드));
		MenuService menuService = new MenuService(menuDao, menuGroupDao, menuProductDao, productDao);

		Menu expectedMenu = mock(Menu.class);
		when(expectedMenu.getId()).thenReturn(1L);
		when(expectedMenu.getName()).thenReturn("후라이드치킨");
		when(expectedMenu.getPrice()).thenReturn(BigDecimal.valueOf(16000));
		when(expectedMenu.getMenuGroupId()).thenReturn(한마리메뉴_id);

		when(menuDao.save(후라이드치킨)).thenReturn(expectedMenu);

		// when
		Menu created_후라이드치킨 = menuService.create(후라이드치킨);

		// then
		assertThat(created_후라이드치킨.getId()).isNotNull();
		assertThat(created_후라이드치킨.getName()).isEqualTo(후라이드치킨.getName());
		assertThat(created_후라이드치킨.getPrice()).isEqualTo(후라이드치킨.getPrice());
		assertThat(created_후라이드치킨.getMenuGroupId()).isEqualTo(후라이드치킨.getMenuGroupId());
	}

	@DisplayName("메뉴의 가격은 0원 이하일 수 없다")
	@Test
	void priceMustOverZero() {
		// given
		Long 두마리메뉴_id = 1L;
		Long 한마리메뉴_id = 2L;
		when(두마리메뉴.getId()).thenReturn(두마리메뉴_id);
		when(한마리메뉴.getId()).thenReturn(한마리메뉴_id);
		Menu 돈주고파는후라이드치킨 = new Menu("후라이드치킨", BigDecimal.valueOf(-10000), 한마리메뉴_id);
		MenuService menuService = new MenuService(menuDao, menuGroupDao, menuProductDao, productDao);

		// when - then
		assertThatThrownBy(() -> {
			menuService.create(돈주고파는후라이드치킨);
		}).isInstanceOf(IllegalArgumentException.class);

	}

	@DisplayName("메뉴는 존재하는 메뉴그룹에만 등록할 수 있다.")
	@Test
	void menuGroupMustExist() {
		// given
		Long invalidMenuGroupId = 3L;
		Menu 후라이드치킨 = new Menu("후라이드치킨", BigDecimal.valueOf(16000), invalidMenuGroupId);

		when(menuGroupDao.existsById(후라이드치킨.getMenuGroupId())).thenReturn(false);
		MenuService menuService = new MenuService(menuDao, menuGroupDao, menuProductDao, productDao);

		// when - then
		assertThatThrownBy(() -> {
			menuService.create(후라이드치킨);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("메뉴의 가격은 메뉴내 상품들의 가격 합보다 클 수 없다.")
	@Test
	void menuPriceCannotOverProduct() {
		// given
		Long 두마리메뉴_id = 1L;
		Long 한마리메뉴_id = 2L;
		when(두마리메뉴.getId()).thenReturn(두마리메뉴_id);
		when(한마리메뉴.getId()).thenReturn(한마리메뉴_id);

		Menu 후라이드치킨 = mock(Menu.class);
		when(후라이드치킨.getPrice()).thenReturn(BigDecimal.valueOf(5000000));
		when(후라이드치킨.getMenuGroupId()).thenReturn(한마리메뉴_id);

		Product 후라이드 = mock(Product.class);
		when(후라이드치킨.getPrice()).thenReturn(BigDecimal.valueOf(16000));

		when(menuGroupDao.existsById(후라이드치킨.getMenuGroupId())).thenReturn(true);
		MenuService menuService = new MenuService(menuDao, menuGroupDao, menuProductDao, productDao);

		// when - then
		assertThatThrownBy(() -> {
			menuService.create(후라이드치킨);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("메뉴의 목록을 조회할 수 있다.")
	@Test
	void list() {
		// given
		Menu menu = mock(Menu.class);
		when(menu.getId()).thenReturn(1L);

		MenuProduct menuProduct = mock(MenuProduct.class);

		when(menuDao.findAll()).thenReturn(Arrays.asList(menu));
		when(menuProductDao.findAllByMenuId(menu.getId())).thenReturn(Arrays.asList(menuProduct));
		MenuService menuService = new MenuService(menuDao, menuGroupDao, menuProductDao, productDao);

		// when
		List<Menu> menus = menuService.list();

		// then
		assertThat(menus).contains(menu);
	}

}