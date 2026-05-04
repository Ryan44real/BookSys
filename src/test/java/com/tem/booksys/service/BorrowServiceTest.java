package com.tem.booksys.service;

import com.tem.booksys.entity.BorrowRecord;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.Impl.BorrowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("借阅服务测试")
class BorrowServiceTest {

    @Mock
    private BorrowMapper borrowMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private BorrowRecord record;

    @BeforeEach
    void setUp() {
        record = new BorrowRecord();
        record.setId(1);
        record.setBookNum("100001");
        record.setUserId(1);
        record.setBorrowState(1);
        record.setBorrowDate(new Date());
    }

    @Test
    @DisplayName("查找用户逾期或超量记录")
    void findOverdueOrMax_shouldReturnRecords() {
        when(borrowMapper.findOverdueOrMax(1)).thenReturn(Arrays.asList(record));

        List<BorrowRecord> records = borrowService.findOverdueOrMax(1);

        assertThat(records).hasSize(1);
        verify(borrowMapper).findOverdueOrMax(1);
    }

    @Test
    @DisplayName("归还图书")
    void returnBook_shouldUpdateState() {
        borrowService.returnBook(1, 100001);

        verify(bookMapper).updateOne(100001, "可借阅");
        verify(borrowMapper).returnBook(100001, 1);
    }

    @Test
    @DisplayName("申请续借")
    void applyRenewal_shouldAddRecord() {
        borrowService.applyRenewal(100001, 1, 7, 1);

        verify(borrowMapper).addApplyRenewal(eq(100001), eq(1), eq(7), eq(1), eq(1));
    }
}
