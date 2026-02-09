package cc.mrbird.febs.cos.entity.vo;

import lombok.Data;

@Data
public class Dispatch {

    private Integer targetPharmacyId;

    private Integer sourcePharmacyId;

    private String medicines;
}
