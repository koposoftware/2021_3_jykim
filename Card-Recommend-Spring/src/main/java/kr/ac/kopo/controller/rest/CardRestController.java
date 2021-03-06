package kr.ac.kopo.controller.rest;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.kopo.service.ICardService;
import kr.ac.kopo.service.IMypageService;
import kr.ac.kopo.vo.MemberVO;
import kr.ac.kopo.vo.card.CardSearchOptionVO;
import kr.ac.kopo.vo.card.CardVO;
import kr.ac.kopo.vo.card.ConsumptionChartVO;
import kr.ac.kopo.vo.card.DibsVO;
import kr.ac.kopo.vo.trans.BenefitParamsVO;
import kr.ac.kopo.vo.trans.BenefitResultVO;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

@RestController
@RequestMapping("/api")
public class CardRestController {
	
	@Autowired
	@Qualifier("cardServiceImpl")
	private ICardService cardService;
	
	@Autowired
	@Qualifier("mypageServiceImpl")
	private IMypageService mypageService;
	
	//GET 카드 검색 결과
	@GetMapping("/card")
	public List<CardVO> getCardSearchList(CardSearchOptionVO searchOption){
		List<CardVO> cards = cardService.searchAllCard(searchOption);
		
		return cards;
	}
	
	//GET 찜카드 검색 결과 => 카드 이미지, 카드 이름, 카드 아이디 리스트
	@GetMapping("/card/dibs")
	public List<DibsVO> getDibsCardSearchList(@RequestParam("dibsLit") String dibsList){
		List<DibsVO> dibsCardList = mypageService.searchDibsCard(dibsList);
		
		return dibsCardList;
	}

	//GET 카드 추천 검색 결과
	@GetMapping("/card/reco/{recoCondition}")
	public List<CardVO> getRecoCardSearchList(@PathVariable("recoCondition") String condition){
		
		List<CardVO> cards = cardService.searchAllRecoCard(condition);
		
		return cards;
	}

	//GET 소비내역 기반 맞춤 카드 추천 검색 결과
	@GetMapping("/card/myreco/{benefitType}")
	public List<CardVO> getMyrecoCardSearchList(@PathVariable("benefitType") String benefitType, Authentication authentication){
		
		List<CardVO> cards = null;
		if(authentication != null && authentication.getPrincipal() instanceof MemberVO) {//로그인 했고, 일반유저라면
			cards = cardService.searchMyRecoCards(((MemberVO) authentication.getPrincipal()).getMemberId(), benefitType);
		}
		
		return cards;
	}

	//GET 보유카드 소비내역 데이터 
	@GetMapping("/mypage/card/consumption/{startDate}/{endDate}")
	public List<ConsumptionChartVO> getMyConsumption(@PathVariable("startDate") String start
													, @PathVariable("endDate") String end
													, Authentication authentication){
		
		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
		List<ConsumptionChartVO> statistics = mypageService.searchMyConsumption(memberId, start, end);
		
		return statistics;
	}

	//GET 찜카드 혜택내역 데이터 
	@GetMapping("/mypage/card/benefit/{startDate}/{endDate}")
	public List<BenefitResultVO> getMyConsumptionBenefit(@PathVariable("startDate") String start
			, @PathVariable("endDate") String end
			, Authentication authentication){
		
		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
		List<BenefitResultVO> benefits = mypageService.searchMyConsumptionBenefit(memberId, start, end);
		
		return benefits;
	}

	//GET 찜한 신용카드 혜택내역 데이터 
	@GetMapping("/mypage/card/credit/dibs/benefit/{cardId}/{startDate}/{endDate}")
	public List<BenefitResultVO> getDibsConsumptionBenefit(@PathVariable("cardId") String cardId, @PathVariable("startDate") String start
			, @PathVariable("endDate") String end
			, Authentication authentication){
		
		
		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
		
		BenefitParamsVO params = new BenefitParamsVO();
		params.setMemberId(memberId);
		params.setCardId(cardId);
		params.setStart(start);
		params.setEnd(end);
		params.setBenefitType(0);
		
		List<BenefitResultVO> benefits = mypageService.searchDibsConsumptionBenefit(params);
		
		return benefits;
	}
	
	//GET 신용카드 top10 
	@GetMapping("/mypage/card/credit/top10/{benefitType}")
	public Map<String, Object> getCreditCardTop10(@PathVariable("benefitType") int benefitType
			, Authentication authentication){
		
		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
		
		//3개월치 소비내역 기반
		LocalDate cur = LocalDate.now();
		LocalDate lastMonth = cur.minusMonths(1);
		LocalDate threeAgoMonth = cur.minusMonths(3);
		
		BenefitParamsVO params = new BenefitParamsVO();
		params.setMemberId(memberId);
		params.setStart(String.valueOf(threeAgoMonth.getYear()) + "-" + String.format("%02d", threeAgoMonth.getMonthValue()));
		params.setEnd(String.valueOf(lastMonth.getYear()) + "-" + String.format("%02d", lastMonth.getMonthValue()));
		params.setBenefitType(benefitType);
		
		Map<String, Object> top10 = mypageService.searchCreditTop10Benefit(params);
		
		return top10;
		
	}
	
	//GET 멀티카드 top3 
	@GetMapping("/mypage/card/multi/top3")
	public Map<String, Object> getMultiCardTop3(Authentication authentication){
		
		
		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
		
		//3개월치 소비내역 기반
		LocalDate cur = LocalDate.now();
		LocalDate lastMonth = cur.minusMonths(1);
		LocalDate threeAgoMonth = cur.minusMonths(3);
		
		BenefitParamsVO params = new BenefitParamsVO();
		params.setMemberId(memberId);
		params.setStart(String.valueOf(threeAgoMonth.getYear()) + "-" + String.format("%02d", threeAgoMonth.getMonthValue()));
		params.setEnd(String.valueOf(lastMonth.getYear()) + "-" + String.format("%02d", lastMonth.getMonthValue()));
		params.setBenefitType(0);
		
		Map<String, Object> top3 = mypageService.searchMultiTop3Benefit(params);
		
		return top3;
		
	}

	//GET 찜한 멀티카드 소비내역 데이터 
	@GetMapping("/mypage/card/multi/dibs/benefit/{cardId}/{startDate}/{endDate}")
	public List<BenefitResultVO> getMultiDibsConsumptionBenefit(@PathVariable("cardId") String cardId, @PathVariable("startDate") String start
			, @PathVariable("endDate") String end
			, Authentication authentication){
		
		
		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
		
		BenefitParamsVO params = new BenefitParamsVO();
		params.setMemberId(memberId);
		params.setCardId(cardId);
		params.setStart(start);
		params.setEnd(end);
		
		List<BenefitResultVO> benefits = mypageService.searchMultiDibsConsumptionBenefit(params);
		
		return benefits;
	}

	//GET Top3 Card 데이터 
	@GetMapping("/mypage/card/reco/{workSector1Code}")
	public List<CardVO> getTop3CardList(@PathVariable("workSector1Code") int sector1, Authentication authentication){
		List<CardVO> cards = null;
		cards = mypageService.searchSector1RecoCards(sector1);
		
		return cards;
	}
	
	//GET 카드 찜하기
//	@PostMapping("/mypage/card/{cardId}")
//	public void dibsCard(@PathVariable("cardId") int cardId, Authentication authentication){
//		int memberId = ((MemberVO) authentication.getPrincipal()).getMemberId();
//		mypageService.dibsCard(memberId, cardId);
//		
//	}
	
	//GET 카드 상세
	@GetMapping("/card/detail/{cardId}")
	public CardVO getCardDetail(@PathVariable("cardId") int cardId, Authentication authentication){
		
		CardVO card = cardService.searchCardDetail(cardId);
		
		
		
		return card;
	}

	//GET 카드 마케팅 대상 고객 리스트
	@GetMapping("/card/admin/marketing/{cardId}")
	public List<MemberVO> getCardMarketingCustomerList(@PathVariable("cardId") int cardId, Authentication authentication){
		
		List<MemberVO> members = cardService.searchCustomerList(cardId);
		
		return members;
	}

	//GET 카드 마케팅 대상 문자 전송
	@GetMapping("/card/admin/marketing/message/{cardId}")
	public void sendCardMarketingMessage(@PathVariable("cardId") int cardId, Authentication authentication){
		
		CardVO card = cardService.searchCardDetail(cardId);
		
		//파일경로
		String path = "C:/develop/spring-final/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/webapps";//이미지 저장 경로
		//String name = ((MemberVO) authentication.getPrincipal()).getName();
		String api_key = "NCSR9KUPGZX308YI";
		String api_secret = "LB8RVXWJRNSWSM4BTMWXXWDQXRCZEC0T";
		Message coolsms = new Message(api_key, api_secret);
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("to", "01094784943");
		params.put("from", "01094784943");
		params.put("type", "MMS");
		params.put("text", "<" + card.getCardName()+">\n-" + card.getCardInfo() + "-\n내 피킹률을 확인하러 가기 => https://www.hanacard.co.kr");
		params.put("image", path + card.getCardImageUrl());//파일경로
		params.put("app_version", "test app 1.2");
		
		try {
			JSONObject obj = (JSONObject) coolsms.send(params);
			System.out.println(obj.toString());
		} catch (CoolsmsException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			System.out.println(e.getCode());
		}
		
	}
	
}
