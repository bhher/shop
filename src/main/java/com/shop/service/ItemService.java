package com.shop.service;

import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemImgDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService;

    private final ItemImgRepository itemImgRepository;
    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        //상품 등록
        Item item = itemFormDto.createItem();
        //상품등록 폼으로 부터 입력 받은 데이터를 이용하여 item 객체를 생성
        itemRepository.save(item); //아이템 데이터를 저장

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            if(i == 0)
                itemImg.setRepimgYn("Y"); //첫번째 이미지를 Y 주고 대표이미지
            else
                itemImg.setRepimgYn("N");
            //상품이미지 정보를 저장
            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }

        return item.getId();
    }
    @Transactional(readOnly = true) //트랜잭션을 읽기전으로 설정
    //jpa가 더티체킹(변경감지)을 수행하지 않아서 성능향상
    public ItemFormDto getItemDtl(Long itemId){
    List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
    //해당상품 이미지를 조회/   등록순으로 가지고 오기위해 상품이미지 아이디 오름차순으로 가지고온다.
     List<ItemImgDto> itemImgDtoList = new ArrayList<>();
    for (ItemImg itemImg : itemImgList) { //조회한 ItemImg 엔티티를 itemImgDto 객체로만들어서
        ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
        itemImgDtoList.add(itemImgDto);// 리스트에 추가 합니다.
    }
        Item item = itemRepository.findById(itemId) //상품에 아이디를 통해 상품엔티티를 조회
                .orElseThrow(EntityNotFoundException::new); //존재하지 않으면 예외발생
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }





    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList)
            throws Exception{
        //상품 수정
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //이미지 등록
        for(int i=0;i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i),
                    itemImgFileList.get(i));
        }

        return item.getId();
    }
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }
//ItemService 클래스에 상품 조회 조건과 페이지 정보를 파라미터로 받아서 상품 데이터를 조회하는
//     getAdminItemPage()메소드를 추가 합니다. 데이터의 수정이 일어나지 않으므로
    //최적화를 위해 @Transactional(readOnly = true) 어노테이션을 설정

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }


}
