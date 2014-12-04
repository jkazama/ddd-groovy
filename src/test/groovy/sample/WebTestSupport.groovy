package sample

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import org.junit.Before
import org.slf4j.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

abstract class WebTestSupport  extends UnitTestSupport {

	protected static final Logger log = LoggerFactory.getLogger("ControllerTest")

	@Autowired
	protected WebApplicationContext wac

	protected MockMvc mockMvc

	@Before
	void before() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
	}

	protected String url(String path) {
		prefix() + path
	}

	protected String prefix() {
		"/"
	}
	
	protected MvcResult performGet(String path) {
		mockMvc.perform(get(url(path))).andExpect(status().isOk()).andReturn()
	}
	
}
