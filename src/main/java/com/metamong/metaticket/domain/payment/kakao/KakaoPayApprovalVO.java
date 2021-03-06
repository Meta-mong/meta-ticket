package com.metamong.metaticket.domain.payment.kakao;

import com.metamong.metaticket.domain.payment.kakao.ApprovalVO.AmountVO;
import com.metamong.metaticket.domain.payment.kakao.ApprovalVO.CardVO;
import lombok.Data;

import java.util.Date;

@Data
public class KakaoPayApprovalVO {

    //response
    private String aid, tid, cid, sid;
    private String partner_order_id, partner_user_id, payment_method_type;
    private AmountVO amount;
    private CardVO card_info;
    private String item_name, item_code, payload;
    private Integer quantity, tax_free_amount, vat_amount;
    private Date created_at, approved_at;
}
