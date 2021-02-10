package com.mmu.fyp.sheetbasedcalculator.calculator;

import com.mmu.fyp.sheetbasedcalculator.BasePresenter;
import com.mmu.fyp.sheetbasedcalculator.BaseView;


public interface CalculatorContract {

    interface Presenter extends BasePresenter{


    }

    interface View extends BaseView<Presenter>{

    }
}
