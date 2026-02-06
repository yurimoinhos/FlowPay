import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserInfoModal } from './user-info-modal';

describe('UserInfoModal', () => {
  let component: UserInfoModal;
  let fixture: ComponentFixture<UserInfoModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserInfoModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserInfoModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
