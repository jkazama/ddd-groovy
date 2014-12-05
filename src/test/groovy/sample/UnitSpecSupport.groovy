package sample

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.transaction.annotation.Transactional

import sample.context.Timestamper;
import sample.context.orm.JpaRepository;
import sample.model.DataFixtures;
import spock.lang.Specification;

//low Spock検証用
@ContextConfiguration(classes = Application, loader = SpringApplicationContextLoader)
@WebAppConfiguration
@DirtiesContext
@Transactional
abstract class UnitSpecSupport extends Specification {

	@Autowired
	protected JpaRepository rep
	@Autowired
	protected DataFixtures fixtures
	@Autowired
	protected Timestamper time
	
}
