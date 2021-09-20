package kr.ac.kopo.service;

import java.util.List;

import kr.ac.kopo.vo.card.CardVO;
import kr.ac.kopo.vo.card.ConsumptionChartVO;
import kr.ac.kopo.vo.card.DibsVO;
import kr.ac.kopo.vo.card.GraphVO;
import kr.ac.kopo.vo.card.MemberCardVO;

public interface IMypageService {
	
	//내 보유 카드 목록
	public List<MemberCardVO> searchMyCards(int memberId);

	//내 소비내역 통계 데이터 => 그래프 그림
	public List<ConsumptionChartVO> searchMyConsumption(int memberId, String start, String end);

	//마이페이지 => sector1 그래프 클릭 시 카드 top3 추천
	public List<CardVO> searchSector1RecoCards(int sector1);

	//찜카드 목록 가져오기
	public List<DibsVO> searchDibsCard(String dibsList);
	
	//카드 찜하기
	//public void dibsCard(int memberId, int cardId);

	
	
	//내 소비내역 통계 데이터 => 그래프 그림
	public List<GraphVO> searchMyConsumption2(String memberId, String cardId , String start, String end);
	
}
