package com.estuate.mpreplica.mapper;

import com.estuate.mpreplica.dto.ProductCreateDto;
import com.estuate.mpreplica.dto.ProductDto;
import com.estuate.mpreplica.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    Product productCreateDtoToProduct(ProductCreateDto dto);
    ProductDto productToProductDto(Product p);
    void updateProductFromDto(ProductCreateDto dto, @MappingTarget Product entity);
}

