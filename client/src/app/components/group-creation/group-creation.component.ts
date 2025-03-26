import { Component, inject, OnInit } from '@angular/core';
import { Concert } from '../../models/concert';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConcertService } from '../../services/concert.service';
import { GroupService } from '../../services/group.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Group } from '../../models/group.models';
import { CreateGroupRequest } from '../../models/create-group-request';

@Component({
  selector: 'app-group-creation',
  standalone: false,
  templateUrl: './group-creation.component.html',
  styleUrl: './group-creation.component.css'
})
export class GroupCreationComponent implements OnInit {
  groupForm!: FormGroup;
  concert: Concert | null = null;
  isLoading = false;
  submitting = false;
  error = '';
  concertDates: string[] = [];

  private fb = inject(FormBuilder);
  private concertSvc = inject(ConcertService);
  private groupSvc = inject(GroupService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  
  ngOnInit(): void {
    this.initForm();
    this.loadConcertDetails();
  }

  private initForm(): void {
    this.groupForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(500)]],
      concertDate: ['', Validators.required],
      capacity: [10, [Validators.required, Validators.min(2), Validators.max(50)]],
      isPublic: [true] // We just set it to public all the time, no point making private groups on this app for now
      // maybe future feature?
    });
  }

  private loadConcertDetails(): void {
    this.isLoading = true;
    const concertId = this.route.snapshot.paramMap.get('id');

    if (!concertId) {
      this.error = 'No concert ID provided';
      this.isLoading = false;
      this.router.navigate(['/groups']);
    }

    this.concertSvc.getConcertById(Number(concertId)).subscribe({
      next: (concert) => {
        this.concert = concert;
        this.concertDates = concert.dates.map(date => new Date(date).toISOString().split('T')[0]);

        // If there's only one date, select it automatically
        if (this.concertDates.length === 1) {
          this.groupForm.get('concertDate')?.setValue(this.concertDates[0]);
        }

        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load concert details. Please try again.';
        this.isLoading = false;
        console.error('Error loading concert', err);
      }
    });
  }

  onSubmit(): void {
    if (this.groupForm.invalid || !this.concert) {
      return;
    }

    this.submitting = true;

    const createRequest: CreateGroupRequest = {
      name: this.groupForm.value.name,
      description: this.groupForm.value.description,
      concertId: this.concert.id,
      concertDate: this.groupForm.value.concertDate,
      capacity: this.groupForm.value.capacity,
      isPublic: this.groupForm.value.isPublic
    };

    console.info("Creating group: ", createRequest);

    this.groupSvc.createGroup(createRequest).subscribe({
      next: (createdGroup) => {
        this.snackBar.open('Group created successfully!', 'Close', { duration: 3000 });
        this.submitting = false;
        this.router.navigate(['/groups', createdGroup.id]);
      },
      error: (err) => {
        this.submitting = false;

        if (err.error?.errors?.group_limit_exceeded) {
          this.snackBar.open('You have reached your group creation limit. Upgrade to premium for more groups.', 'Close', { duration: 5000 });
        } else if (err.error?.errors?.invalid_concert_date) {
          this.snackBar.open('Invalid concert date selected.', 'Close', { duration: 3000 });
        } else {
          this.snackBar.open('Failed to create group. Please try again.', 'Close', { duration: 3000 });
        }

        console.error('Error creating group', err);
      }
    });
  }

  get nameErrors(): string {
    const control = this.groupForm.get('name');
    if (control?.errors?.['required']) return 'Group name is required';
    if (control?.errors?.['minlength']) return 'Group name must be at least 5 characters';
    if (control?.errors?.['maxlength']) return 'Group name cannot exceed 100 characters';
    return '';
  }

  get descriptionErrors(): string {
    const control = this.groupForm.get('description');
    if (control?.errors?.['required']) return 'Description is required';
    if (control?.errors?.['minlength']) return 'Description must be at least 20 characters';
    if (control?.errors?.['maxlength']) return 'Description cannot exceed 500 characters';
    return '';
  }

  get capacityErrors(): string {
    const control = this.groupForm.get('capacity');
    if (control?.errors?.['required']) return 'Capacity is required';
    if (control?.errors?.['min']) return 'Capacity must be at least 2';
    if (control?.errors?.['max']) return 'Capacity cannot exceed 50';
    return '';
  }
}
